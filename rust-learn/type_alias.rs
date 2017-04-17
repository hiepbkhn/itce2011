type Name = String;

fn main() {
    let x: Name = "Hello".to_string();

    println!("{}", x);

    let x: i32 = 5;
    let y: i64 = 5;

    if x == (y as i32) {
    // ...
    }
}