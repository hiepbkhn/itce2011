// fn main() {
//     let v = vec![1, 2, 3];

//     let mut v2 = v;

//     println!("v[0] is: {}", v[0]);
// }


fn main() {
    let a = 5;

    let _y = double(a);
    println!("{}", a);
}

fn double(x: i32) -> i32 {
    x * 2
}