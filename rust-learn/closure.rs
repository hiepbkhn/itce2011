// fn main() {

//     let nums = vec![1, 2, 3];

//     // let takes_nums = || nums;

//     println!("{:?}", nums);
// }

////////////////////
// fn call_with_one<F>(some_closure: F) -> i32
//     where F: Fn(i32) -> i32 {

//     some_closure(1)
// }

// fn main(){
//     let answer = call_with_one(|x| x + 2);
//     assert_eq!(3, answer);
// }


////////////////////
// fn call_with_one(some_closure: &Fn(i32) -> i32) -> i32 {
//     some_closure(1)
// }

// fn add_one(i: i32) -> i32 {
//     i + 1
// }

// fn main() {
//     let f = add_one;

//     let answer = call_with_one(&f);
//     // let answer = call_with_one(&add_one);

//     assert_eq!(2, answer);
// }


///////////////// Returning closures
fn factory() -> Box<Fn(i32) -> i32> {
    let num = 5;

    Box::new(move |x| x + num)
}

fn main() {
    let f = factory();

    let answer = f(1);
    assert_eq!(6, answer);
}