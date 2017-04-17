const N: i32 = 5;

static M: i32 = 10;

static mut O: i32 = 15;

fn main() {
    println!("{}", N);
    println!("{}", M);
    unsafe {
        O += 1;

        println!("O: {}", O);
    }
}