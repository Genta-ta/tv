use std::io::{Read, Write};
use std::net::TcpStream;

fn url_encode(s: &str) -> String {
    s.chars().map(|c| match c {
        'a'..='z'|'A'..='Z'|'0'..='9' => c.to_string(),
        ' ' => "+".into(),
        _   => format!("%{:02X}", c as u8),
    }).collect()
}

fn http_get(host: &str, port: u16, path: &str) -> Option<String> {
    let mut s = TcpStream::connect((host, port)).ok()?;
    write!(s, "GET {} HTTP/1.0\r\nHost: {}\r\nConnection: close\r\n\r\n",
           path, host).ok()?;
    let mut buf = String::new();
    s.read_to_string(&mut buf).ok()?;
    buf.find("\r\n\r\n").map(|i| buf[i+4..].to_string())
}

fn json_str<'a>(json: &'a str, key: &str) -> Vec<&'a str> {
    let needle = format!("\"{}\":\"", key);
    let mut out = Vec::new();
    let mut pos = 0;
    while let Some(i) = json[pos..].find(&needle) {
        let start = pos + i + needle.len();
        if let Some(end) = json[start..].find('"') {
            out.push(&json[start..start+end]);
            pos = start + end;
        } else { break; }
        if out.len() >= 3 { break; }
    }
    out
}

fn search(query: &str) -> String {
    let path = format!("/search?q={}&format=json", url_encode(query));
    match http_get("localhost", 8080, &path) {
        Some(body) => {
            let titles   = json_str(&body, "title");
            let contents = json_str(&body, "content");
            titles.iter().zip(contents.iter())
                .map(|(t, c)| format!("[{}] {}", t, c))
                .collect::<Vec<_>>()
                .join("\n")
        }
        None => "no results".into(),
    }
}

fn main() {
    let q: Vec<String> = std::env::args().skip(1).collect();
    if q.is_empty() {
        eprintln!("usage: search <query>");
        std::process::exit(1);
    }
    println!("{}", search(&q.join(" ")));
}
