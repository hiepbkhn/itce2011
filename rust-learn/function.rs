
fn plus_one(i: i32) -> i32 {
    i + 1
}


fn main() {
    // Without type inference:
    // let f: fn(i32) -> i32 = plus_one;

    // With type inference:
    let f = plus_one;

    let six = f(5);
    println!("six = {}", six);
    
}

