# marsrover

## How to run it

There is a pre-compiled binary that exists in the /target directory

```
java -jar target/marsrover-0.1.0-SNAPSHOT-standalone.jar
```

Will give you the default response based on the instructions provided in resource/instructions.txt. Alternative instructions may be used my overriding the file input flag

```
java -jar target/marsrover-0.1.0-SNAPSHOT-standalone.jar -f foo.txt
```

Alternatively a help flag will print out available options

```
java -jar target/marsrover-0.1.0-SNAPSHOT-standalone.jar -h
```

This application does require the JVM, start up times are a little slow. Please be patient. Clojure dynamically loads libraries into the JVM, at runtime making this not the best use case for it as a language. However, I think I could port this to a ClojureScript app pretty quickly. One of the benefits of Clojure is that once everything is compiled it is pretty quick.

## Validation

This application uses clojure.spec for input validation, it is a composable validation framework. For validation I check types for cartesion co-ordinates, instructions and direction. I further check that instructs and directions are limited to the subset of the alphabet which we use to represent them.


## Out of Bounds

Instructions for design were ambigious on what to do for an issue where instructions may take the rover out of bounds. I see two potential ways of handling this:

* Play the entire instruction set forward to see if the rover will go out of bounds before moving it, and if it does leave it where it is.
* Move the rover to a point where it would go out of bounds and then return it's new position to ground control.

I believe the correct approach is to see if it does go out of bounds at any stage, and if it does leave it where it is. The down side to this approach is that it means that I am traversing the entire instruction set twice. Once as a test, the second time in order to move the rover. My choice on going this route rather than on returning a new position is that I have less faith in our hypothetical communication system, and expect dropped packets. If the rover drops the packet on it's new location, it could well be lost. As such if a rover moves out of the pre-defined grid I immedeatly throw an exception and the command to move the rover never gets run.

## Movement

Movement is based on reading the tape at each step and making a decision based on the input, recurring through an instruction parser at each step.

## Exception Handling

Catching Exceptions in this way is not a true functional paradigm. You will notice that most Clojure programs are not written like this. Given that we are dealing with IO (and given the small size of the program), I opted to use exceptional handling which is closer to the imperative style of coding. Mostly because I could delegate error handling back to the JVM.

## Code correctness

Clojure development is tightly coupled to the repl, as I build each function I test it against a running background process with some dummy data. This short feedback cycle gives the ability to correct in the small before program complexity takes over.

## Code Structure

It's not normal to have everything inside a single namespace, and a structure like this is more applicable to a code challenge than it is an actual app. However, given the small size of the code base I believe that having everything in a single file increases readability and as such reduces cognitive overhead, increasing the understandability of the code. While I could split this codebase into several files for validation, movement, boundary-detection, or application execution. I think that this project is small enough to warrant a single file.

Most design decisions were might for a) robustness b) getting out of the way of the developer and getting the job done.
