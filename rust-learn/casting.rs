
fn main() {
    // let one = true as u8;
    // let at_sign = 64 as char;
    // let two_hundred = -56i8 as u8;

    let a = 300 as *const char; // `a` is a pointer to location 300.
    let b = a as u32;
    println!("{}", b);
}