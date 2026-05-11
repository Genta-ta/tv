use reqwest;
use scraper::{Html, Selector};
use std::env;

fn main() {
    // Ambil query dari argumen command line
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!("Usage: search <query>");
        return;
    }
    let query = &args[1];

    match search_duckduckgo(query) {
        Ok(results) => println!("{}", results),
        Err(e) => eprintln!("Error searching: {}", e),
    }
}

fn search_duckduckgo(query: &str) -> Result<String, Box<dyn std::error::Error>> {
    let url = format!("https://html.duckduckgo.com/html/?q={}", query);
    
    // Setup client dengan User-Agent agar tidak diblokir
    let client = reqwest::blocking::Client::builder()
        .user_agent("Mozilla/5.0")
        .build()?;

    let html = client.get(url).send()?.text()?;
    let document = Html::parse_document(&html);
    
    // Selector untuk cuplikan hasil pencarian di DuckDuckGo HTML
    let selector = Selector::parse(".result__snippet").unwrap();
    
    let mut context = String::new();
    for element in document.select(&selector).take(3) { // Ambil 3 hasil teratas
        context.push_str(&element.text().collect::<Vec<_>>().join(" "));
        context.push_str("\n");
    }

    Ok(context)
}
