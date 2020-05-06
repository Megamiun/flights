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
```json
{
  "origin": "A",
  "end": "C"
}
```

##### OK Response
- Status: 200
- Response Body:
```json
{
  "formattedPath": "A - B - C > $45",
  "path": ["A", "B", "C"],
  "price": 45
}
```

##### "Missing origin or end" Response
- Status: 400
- Response Body:
```json
{ "error": "No 'origin' or 'end' parameter was given" }
```

##### "No route found" Response
- Status: 204

#### `PUT /api/routes`

Allow us to insert new routes to our ever-growing list of routes, with a body containing the two points and the price of travel between them. Oh, and every route is considered to be bidirectional.

What? Authorization validation? No today, sorry. But I am sure you can pretend there is a header with an S2S token, or an admin token if you want. For now our database will be public domain.

##### Request Body
```json
{
  "origin": "A",
  "end": "B",
  "price": 45
}
```

##### OK Response
- Status: 200
- Response Body:
```json
{
  "formattedPath": "A - B - C > $45",
  "path": ["A", "B", "C"],
  "price": 45
}
```

##### "Missing origin or end or price" Response
- Status: 400
- Response Body:
```json
{ "error": "No 'origin' or 'end' or 'price' parameter was given" }
```

##### "No numerical price" Response
- Status: 400
- Response Body:
```json
{ "error": "No numerical 'price' was given" }
```

#### `GET /api/routes?origin=A&end=B`

For those that want to test our APIs directly into your web browser without opening a curl or postman, we too have this GET endpoint that takes query parameters instead of a json body! 

For a better description, just read the POST version and pretend every json parameter is a query parameter.
