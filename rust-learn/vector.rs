fn main() {
    let mut v = vec![1, 2, 3, 4, 5];

    for i in &v {
        println!("A reference to {}", i);
        let i = i * 2;
    }

    for i in &mut v {
        println!("A mutable reference to {}", i);
        // let i = i * 2;
    }

    // for i in v {
    //     println!("Take ownership of the vector and its element {}", i);
    // }
    println!("{}", v[0]);
}