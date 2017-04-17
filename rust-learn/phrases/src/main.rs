// extern crate phrases;

// fn main() {
//     println!("Hello in English: {}", phrases::english::greetings::hello());
//     println!("Goodbye in English: {}", phrases::english::farewells::goodbye());

//     // println!("Hello in Japanese: {}", phrases::japanese::greetings::hello());
//     // println!("Goodbye in Japanese: {}", phrases::japanese::farewells::goodbye());
// }


///////////////////////
extern crate phrases;

use phrases::english::greetings;
use phrases::english::farewells;

fn main() {
    println!("Hello in English: {}", greetings::hello());
    println!("Goodbye in English: {}", farewells::goodbye());
}