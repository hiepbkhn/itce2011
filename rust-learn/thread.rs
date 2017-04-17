// use std::thread;

// fn main() {
//     let handle = thread::spawn(|| {
//         "Hello from a thread!"
//     });

//     println!("{}", handle.join().unwrap());
// }

////////////////
// use std::thread;

// fn main() {
//     let x = 1;
//     thread::spawn(move || {
//         println!("x is {}", x);
//     });
// }


///////////////// Safe Shared Mutable State
// use std::thread;
// use std::time::Duration;

// fn main() {
//     let mut data = vec![1, 2, 3];

//     for i in 0..3 {
//         thread::spawn(move || {
//             data[0] += i;
//         });
//     }

//     thread::sleep(Duration::from_millis(50));
// }


////////
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;

fn main() {
    let data = Arc::new(Mutex::new(vec![1, 2, 3]));

    for i in 0..3 {
        let data = data.clone();
        thread::spawn(move || {
            let mut data = data.lock().unwrap();
            data[0] += i;
        });
    }

    thread::sleep(Duration::from_millis(50));
}
