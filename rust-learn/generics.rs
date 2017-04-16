
// fn main() {
//     let x: Option<i32> = Some(5);
//     let y: Option<f64> = Some(5.0f64);
// }


///////////////////// ERROR
// fn sum<T: Add<T,T>>(x: T, y: T)->T{
//     x+y
// }

// fn main(){
//     println!("{}", sum(5, 6));
// }

//////////////////////
use std::ops::Add;

fn double_it<T>(x: T) -> T
where T: Add<Output=T> + Copy + Clone {
    x + x
}

fn add<T>(x: T, y: T) -> T
where T: Add<Output=T> + Copy + Clone {
    x + y
}

fn main() {
    println!("{}", double_it(21));
    println!("{}", add(21, 33));
}


//////////////////////
// fn main(){
//     // u32 suffix literal denotes an unsigned 32-bit integer; i16 is signed 16-bit.
//     // All other values in the arrays are inferred to be of the same type.
//     let a = find_min(vec![1i32,2,3,4]);
//     let b = find_min(vec![10i16,20,30,40]);
//     match a {
//         Some(value) =>  {println!("{}", value)},
//         None => {},
//     }
//     // println!("{} {}", a, b);
// }

// // Dat Code Reuse
// fn find_min<T: Ord>(data: Vec<T>) -> Option<T> {
//     let mut it = data.into_iter();
//     let mut min = match it.next() {
//         Some(elem) => elem,
//         None => return None,
//     };
//     for elem in it {
//         if elem < min {
//             min = elem;
//         }
//     }
//     Some(min)
// }


////////////////////// ERROR
// use std::num::{One, Zero};
// use std::ops::Mul;
// use std::ops::Sub;

// fn fact<T: Eq + Zero + One + Mul<T, T> + Sub<T, T>>(n: T) -> T {
//     if n == Zero() { One() } else { fact(n - One()) * n }
// }

// fn main() {
//     println!("{}", fact(5));
// }


//////////////////////////
// use std::ops::Sub;

// #[derive(Debug)]
// struct Point {
//     x: i32,
//     y: i32,
// }

// impl Sub for Point {
//     type Output = Point;

//     fn sub(self, other: Point) -> Point {
//         Point {
//             x: self.x - other.x,
//             y: self.y - other.y,
//         }
//     }
// }

// impl PartialEq for Point {
//     fn eq(&self, other: &Self) -> bool {
//         self.x == other.x && self.y == other.y
//     }
// }

// fn main() {
//     assert_eq!(Point { x: 3, y: 3 } - Point { x: 2, y: 3 },
//                Point { x: 1, y: 0 });
// }

