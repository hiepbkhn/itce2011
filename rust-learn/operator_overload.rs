// use std::ops::Add;

// #[derive(Debug)]
// struct Point {
//     x: i32,
//     y: i32,
// }

// impl Add for Point {
//     type Output = Point;

//     fn add(self, other: Point) -> Point {
//         Point { x: self.x + other.x, y: self.y + other.y }
//     }
// }

// fn main() {
//     let p1 = Point { x: 1, y: 0 };
//     let p2 = Point { x: 2, y: 3 };

//     let p3 = p1 + p2;

//     println!("{:?}", p3);
// }


//////////////////// Using operator traits in generic structs
use std::ops::Mul;

trait HasArea<T> {
    fn area(&self) -> T;
}

struct Square<T> {
    x: T,
    y: T,
    side: T,
}

impl<T> HasArea<T> for Square<T>
        where T: Mul<Output=T> + Copy {
    fn area(&self) -> T {
        self.side * self.side
    }
}

fn main() {
    let s = Square {
        x: 0.0f64,
        y: 0.0f64,
        side: 12.0f64,
    };

    println!("Area of s: {}", s.area());
}