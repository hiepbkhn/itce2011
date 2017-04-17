// trait Foo {
//     fn f(&self);
// }

// trait Bar {
//     fn f(&self);
// }

// struct Baz;

// impl Foo for Baz {
//     fn f(&self) {
//         println!("Baz’s impl of Foo");
//     }
// }

// impl Bar for Baz {
//     fn f(&self) {
//         println!("Baz’s impl of Bar");
//     }
// }

// fn main() {
//     let b = Baz;
//     // b.f();
//     Foo::f(&b);
//     Bar::f(&b);
// }


//////////////////// Angle-bracket Form
trait Foo {
    fn foo() -> i32;
}

struct Bar;

impl Bar {
    fn foo() -> i32 {
        20
    }
}

impl Foo for Bar {
    fn foo() -> i32 {
        10
    }
}

fn main() {
    assert_eq!(10, <Bar as Foo>::foo());
    assert_eq!(20, Bar::foo());
}