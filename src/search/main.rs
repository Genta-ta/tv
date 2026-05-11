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
    // Timeout agar tidak stuck kalau internet lemot
    s.set_read_timeout(Some(std::time::Duration::from_secs(5))).ok()?;
    
    write!(s, "GET {} HTTP/1.0\r\nHost: {}\r\nUser-Agent: my-ai-agent\r\nConnection: close\r\n\r\n",
           path, host).ok()?;
    
    let mut buf = String::new();
    s.read_to_string(&mut buf).ok()?;
    buf.find("\r\n\r\n").map(|i| buf[i+4..].to_string())
}

fn json_extract(json: &str, key: &str) -> Vec<String> {
    let needle = format!("\"{}\":\"", key);
    let mut results = Vec::new();
    for part in json.split(&needle).skip(1) {
        if let Some(end) = part.find('"') {
            results.push(part[..end].to_string());
        }
        if results.len() >= 3 { break; }
    }
    results
}

fn main() {
    let args: Vec<String> = std::env::args().skip(1).collect();
    if args.is_empty() {
        eprintln!("usage: search <query>");
        return;
    }
    
    let query = args.join(" ");
    let path = format!("/search?q={}&format=json", url_encode(&query));
    
    // Ganti searx.be dengan instance lain jika down
    match http_get("searx.be", 80, &path) {
        Some(body) => {
            let titles = json_extract(&body, "title");
            let contents = json_extract(&body, "content");
            for (t, c) in titles.iter().zip(contents.iter()) {
                println!("[{}] {}", t, c);
            }
        }
        None => println!("Gagal mengambil data dari internet."),
    }
}
