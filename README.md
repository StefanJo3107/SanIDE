# SanIDE

An IDE for [SanUSB](https://github.com/StefanJo3107/SanScript) - open source USB Rubber Ducky-like devices

## Features

- Code editor based on [Monaco Editor](https://microsoft.github.io/monaco-editor/)
- Integration with [SanScript](https://github.com/StefanJo3107/SanScript) compiler and [espflash](https://github.com/esp-rs/espflash)
- Integrated IRC Client

## Getting Started

### Project Overview

- Architecture:
  [Single Page Application (SPA)](https://en.wikipedia.org/wiki/Single-page_application)
- Languages
  - Front end is [ClojureScript](https://clojurescript.org/) with ([re-frame](https://github.com/day8/re-frame))
  - Back end is [Clojure](https://clojure.org/) with [ring](https://github.com/ring-clojure/ring)
- Dependencies
  - UI framework: [re-frame](https://github.com/day8/re-frame)
    ([docs](https://github.com/day8/re-frame/blob/master/docs/README.md),
    [FAQs](https://github.com/day8/re-frame/blob/master/docs/FAQs/README.md)) ->
    [Reagent](https://github.com/reagent-project/reagent) ->
    [React](https://github.com/facebook/react)
  - Back end: [ring](https://github.com/ring-clojure/ring), [http-kit](https://http-kit.github.io/server.html)
  - Process management: [Babashka](https://github.com/babashka/process)
- Build tools
  - CLJS compilation, dependency management, REPL, & hot reload: [`shadow-cljs`](https://github.com/thheller/shadow-cljs)
  - Test runner: [kaocha](https://github.com/lambdaisland/kaocha), [cljs-test-runner](https://github.com/Olical/cljs-test-runner)
  - Benchmarking: [criterium](https://github.com/hugoduncan/criterium) 

#### Directory structure

- [`resources/public/`](resources/public/): SPA root directory;
  - [`index.html`](resources/public/index.html): SPA home page
  - Generated directories and files
    - `js/compiled/`: compiled CLJS (`shadow-cljs`)
      - Not tracked in source control; see [`.gitignore`](.gitignore)
- [`src/cljs/sanide_frontend/`](src/cljs/sanide_frontend/): SPA source files (ClojureScript,
  [re-frame](https://github.com/Day8/re-frame))
  - [`core.cljs`](src/cljs/sanide_frontend/core.cljs): contains the SPA entry point, `init`
- [`src/clj/sanide_backend/`](src/clj/sanide_backend/): Back end source files
  - [`core.clj`](src/clj/sanide_backend/core.cljs): contains the back end entry point, `main`
- [`test/cljs/sanide_frontend/`](src/cljs/sanide_frontend): contains tests for back end
- [`test/clj/sanide_backend/`](src/clj/sanide_backend): contains tests for front end

### Environment Setup

1. Install [JDK 8 or later](https://openjdk.java.net/install/) (Java Development Kit)
2. Install [Node.js](https://nodejs.org/) (JavaScript runtime environment) which should include
   [NPM](https://docs.npmjs.com/cli/npm) or if your Node.js installation does not include NPM also install it.
3. Install [esp-rs](https://docs.esp-rs.org/book/installation/index.html) libraries
4. Clone this repo

## Development

### Running the App

#### Front end

Install npm dependencies and start web server for serving front end:

```sh
npm install
clj -M:frontend
```

Please be patient; it may take over 20 seconds to see any output, and over 40 seconds to complete.

When `[:app] Build completed` appears in the output, browse to
[http://localhost:8280/](http://localhost:8280/).

#### Back end

Start HTTP and WS server:

```sh
clj -M:api
```

Server will start on port 9000 on address [http://localhost:9000](http://localhost:9000). Swagger UI is accessible at [http://localhost:9000/api-docs/index.html](http://localhost:9000/api-docs/index.html).

#### Tests
Start front end tests:

```sh
clj -M:test-frontend
```

Start back end tests:

```sh
clj -M:test-backend
```

Start benchmarking tests:

```sh
clj -M:bench
```

## Showcase
|    |    |
|:-------:|:-------:|
|<img src="https://i.imgur.com/TwlLkIx.png" width="500">|<img src="https://i.imgur.com/Hu0vF5Z.png" width="500">|
|**Title screen**|**Code editor**|
|    |    |
|<img src="https://i.imgur.com/K5do2b5.png" width="500">|<img src="https://i.imgur.com/oCnQv1D.png" width="500">|
|**IRC Title screen**|**IRC Chat**|
