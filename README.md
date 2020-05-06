# Flights

## What is it?

The minimal Kotlin version of a program that finds the minimum price for a tourist wanting to travel from A to Z!

## Running

To run this program, we need to do some things:

- Have a java runtime installed in your computer
- Create a fat jar of the program: `./gradlew shadowJar`
- Go to the folder `build/libs` or copy the jar inside this folder to your desired folder
- Run jar using: `java -jar flights-1.0-SNAPSHOT-all.jar input-routes.csv`

The argument `input-sources.csv` can be any .csv file that has the format `FROM,TO,PRICE`. It will serve as a start to the database and will be updated every time we add a new route.

Oh, if the argument is not given, the program will initialize a default [database](src/main/resources/default.csv) and will update a temp file with the new data.

## How can I use it?

Once you run this program, there will be two ways of interfacing with the program:
 
### CLI

Allow queries about travel routes between two points, with an input with format `FROM-TO`, return the best route from the two points. For example:

```shell
Please enter the route: GRU-CGD
Best route: GRU - BRC - SCL - ORL - CDG > $40
Please enter the route: BRC-CDG
Best route: BRC - ORL > $30
Please enter the route: GRU-NYC
There is no route between the two points. Is there another route you would like to know?
```

As seen above, there is a treatment too for when no route is found, printing an wrror message.

### Rest APIs

There are three endpoints, locked at base url `http://localhost:8000`(Sorry, guys, hope there is nothing running on your port 8000), they are:

#### `POST /api/routes`

Allow queries about travel routes between two points, with a body with the data about the desired start and end of the route.

##### Request Body
```JSON
{
  "origin": "A",
  "end": "C"
}
```

##### OK Response
- Status: 200
- Response Body:
```JSON
{
  "formattedPath": "A - B - C > $45",
  "path": ["A", "B", "C"],
  "price": 45
}
```

##### "Missing origin or end" Response
- Status: 400
- Response Body:
```JSON
{ "error": "No 'origin' or 'end' parameter was given" }
```

##### "No route found" Response
- Status: 204

#### `PUT /api/routes`

Allow us to insert new routes to our ever-growing list of routes, with a body containing the two points and the price of travel between them. Oh, and every route is considered to be bidirectional.

What? Authorization validation? No today, sorry. But I am sure you can pretend there is a header with an S2S token, or an admin token if you want. For now our database will be public domain.

##### Request Body
```JSON
{
  "origin": "A",
  "end": "B",
  "price": 45
}
```

##### OK Response
- Status: 200
- Response Body:
```JSON
{
  "formattedPath": "A - B - C > $45",
  "path": ["A", "B", "C"],
  "price": 45
}
```

##### "Missing origin or end or price" Response
- Status: 400
- Response Body:
```JSON
{ "error": "No 'origin' or 'end' or 'price' parameter was given" }
```

##### "No numerical price" Response
- Status: 400
- Response Body:
```JSON
{ "error": "No numerical 'price' was given" }
```

#### `GET /api/routes?origin=A&end=B`

For those that want to test our APIs directly into your web browser without opening a curl or postman, we too have this GET endpoint that takes query parameters instead of a JSON body! 

For a better description, just read the POST version and pretend every JSON parameter is a query parameter.


## Design - TL;DR Edition

### Packages

- `br.com.gabryel.flights` - The base package of our application, only has the Main class inside of it, initializes and orchestrates the application. Old Spring habits die hard.
- `br.com.gabryel.flights.commons` - The core package of the application, has everything that is interface agnostic.
- `br.com.gabryel.flights.cli` - The package focused in creating a CLI Client for accessing our data, later the internal were changed to the prefix `Stream` so we could test the CLI behaviour without using our CLI.
- `br.com.gabryel.flights.rest` - The package focused in creating a REST API for accessing our data. 
- `br.com.gabryel.flights.rest.model` - Where we have all our DTOs for the REST APIs.

## Design - Almost An Article Edition

Ok, this design has really changed from the start, but I will try to review its evolution.

### RouteFinder - A Start

As I started, I perceived one thing: It would be simpler to start developing the core of my application, the RouteManager.

No, nothing of CLI, nothing of REST or things of the type, the first step was to design the algorithm, more specifically, the `findRoute` method.

As soon as I saw the problem, I perceived it would be a graph problem and as almost always: if you gotta a minimal distance graph problem, you will find your answers in Dijkstra.

But then, I had to think about the output, which would be the better way to return the result? For starters, there is always the chance there is no path between the two points, so I already decided that the method will return a nullable answer.

After that, I had to think of a flexible way to return the output, so I decided for a nullable pair of a `Path`, a structure like a linked list, and an `Int`, containing the total value.

Once we defined our inputs and outputs, I defined I would maintain a local cache of all registered routes, so I didn't had to consult the file every time and that every search I made was the most optimized I could get. The format was a map inside a map, both indexed by a `String`, containing a list of routes from one to another. This decision made it easy to discover the links between the nodes for the Dijkstra algorithm and is pretty performatic.

### Interacting with the user - CLI

After this, I wanted a way to test the program, and what best way than to look at the spec and implement the simplest possible interface for it?

After thinking a little, I saw the task would be really simple, just write a message requesting for user queries BUT DON'T PRINT A NEW LINE and... wait for the user to finish his line, after that parsing this input and searching using our beautiful `RouteFinder` and printing it, even adding an error message for paths not found.

And sincerely? This was it, for now, the only other thing I did was to create our [default database file](src/main/resources/default.csv) and in case of the database starter argument not being given, using it, so I would not need to input this argument every time.

### Interacting with the user - REST API

And now was the time for the truth, could I make a REST API using no frameworks or extra packages? The response was: no without googling.

Even so, after searching for some minutes, I discovered that the Java HTTPServer APIs were not so horrible as I had feared, they were mostly intuitive... Emphasis on the `mostly`, but usually they don't are just a little too basic, like having to check the HTTP method manually, or instead of returning a Response object, having the responsibility of causing collateral effects myself.

After some more learning, I put my engineer hat back on again and started to decide the API contracts. The first thing I decided was that I wanted a simple GET endpoint without a payload, so I could test it on the simplest terms. This endpoint was called `/api/routes` and received our parameters as query parameters.

This endpoint was quite simple, I decided I would define some basic answers, like returning a 400 for missing data, or a 204 for no route found, and obviously, a success response with a JSON payload.

### Interacting with the user - REST API - The JSON Menace

And it was time, time to face the truth: I was determined to use no libraries outside of the Kotlin stdlib. But even so, creating a JSON parser would probably be the flimsiest part of my application and probably even take half of the code of the project.

So I decided to make a single exception for this, I added Gson to my dependencies like who was carrying the world in my back and nobody could judge me. Sorry, world.

After that, everything started to run smoothly again, now armed with JSON, I could make the same GET endpoint as a POST endpoint, not having to depend on our query params. The contract continued the same as the GET, as both even share the same code for validation and response writing.

### Interacting with the user - REST API - From RouteFinder to RouteManager

And lastly, I would define the PUT endpoint, after everything I got through, it was very simple. By this time I had already some auxiliary methods that wrote exactly what I wanted to the response. The only question would be evolving my `RouteFinder` to a `RouteManager` and adding the responsibility of writing to our database too. Very very anti-climatic.

### ~~The Ending~~ - Putting It All Together

After all that, all I needed to do was create and orchestrator to our application, managing both the REST API and the CLI client, and this was how the almighty `Main.kt` was born, making sure to initialize all components and start our database, setting up our CLI and REST servers and running them.

### ~~The Ending~~ - Tests

As a programmer that is hungry to code our solutions and see our code running, I left the tests to the end. Even though it is not TDD if done after the feature is ready, I find that writing tests really helps me to validate everything that I made and refactoring the application in a way that makes it more modular.

Thanks to that, making some BDD style tests, I discovered various mini flaws in my application and solved them.

Thanks to that, not being able to make some BDD style tests, I discovered various ways to make the application more modular, like breaking the RestServer into a Server and a Handler, simplifying the process of both testing and separating the responsibilities.

### ~~The Ending~~ - Rereading the spec

Oh, did you know that I had to update the original input file with the new given routes? I didn't remember it until rereading the spec, so I think it is back to the drawing board.

Fortunately, it was simpler than I thought to solve this, I just had to create an OutputStream of the same file I selected and write it in the `RouteManager`.

### The Ending - Writing the README

It was a very fun road writing this README, it really puts everything in perspective and even gave me some ideas to better this project in the future.

### The Future - Better Faster Harder Stronger

As I wrote this I perceived some things that could be better:

- [ ] `RouterManager` - I don't need to have a map of maps to help my Dijkstra implementation.
- [ ] `RouterManager` - Probably I should have a database class that is between the file and the Manager, so responsibilities are better distributed.
- [ ] `StreamServer`/`StreamHandler` - Although this way it was better for testing, I still think neither my implementation nor tests are ideal in some scenarios. I still don't have the knowledge to emulate perfectly a CLI in my tests, although with some debugging maybe I can do that with temporary files.
- [ ] `RoutesServer`/`RoutesHandler` - So much better than my `StreamServer`, even so, maybe it would be interesting to have different paths for the GET/POST and the PUT.