# JobTech Taxonomy API

The JobTech Taxonomy API is a REST API for the [JobTech Taxonomy
Database][0]. The JobTech Taxonomy Database contains terms or phrases used
at the Swedish labour market.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

You will also need a [Datomic][2] database with the JobTech Taxonomy Database installed:
<https://github.com/JobtechSwe/jobtech-taxonomy-database

Set your connection details in `env/dev/resources/config.edn`.

## Running

The project is based on [Luminus][3], which provides the web server
infrastructure.

You can either run Luminus from either your terminal or from a repl.

Regardless of how you start the project, don't forget to start the
Datomic proxy script first, in case you use Datomic Cloud:

    https://docs.datomic.com/cloud/getting-started/connecting.html

### Controlling from Terminal

Change directory to the project root folder.

To start a web server for the application, run this
to connect to AWS Datomic:

    lein run -p 3000

To use a local Datomic, run:

    lein with-profile local run -p 3000


### Controlling from nREPL

Change to the project root folder and start your repl (If you use IntelliJ,
don't forget to load the project in your repl).

It is recommended to keep a file `dev-config.edn`, see "CREATE dev-config for local developement below".

To start the HTTP server and any other components such as databases,
run the start function in your repl:

    (start)


## Next step
Then open the following URL in a web browser:

    http://localhost:3000/v1/taxonomy/swagger-ui/index.html

## Authorize
Click the Authorize button, and enter your test account code,
defined in `env/dev/resources/config.edn`.

## Running a query

    curl -X GET --header 'Accept: application/json' --header 'api-key: TEST-ACCOUNT-CODE' 'http://localhost:3000/v1/taxonomy/main/concepts?preferred-label=Danska'

## Testing
The integration test setup creates a temporary database for each test,
which makes it safe to do any modifications without leaving traces
behind.

Summary:
 - test runner: Kaocha (https://github.com/lambdaisland/kaocha).
 - test command: `lein kaocha --focus-meta TAG`
   where TAG is the name of one of the test's tags (such as `integration`).
 - status: for integration tests that rely on a live database, only one test
   can be run at a time. This means that you should assign each test a unique
   tag (e.g. `(test/deftest ^:changes-test-2 changes-test-2 ...)`), and then
   run it with `lein kaocha --focus-meta changes-test-2`.

### How to write an integration test

#### File and namespace
Your test should reside in the directory `test/clj/jobtech_taxonomy_api/test/`.

You should either pick an existing file, or create a new file, ending
with `_test.clj`.  It should use a namespace like this: `(ns
jobtech-taxonomy-api.test.FILENAME ...)`, where FILENAME is for example
`changes-test`.

You need to require `[jobtech-taxonomy-api.test.test-utils :as util]`.

#### Define fixtures
Place one occurance of this line in your test file:
`(test/use-fixtures :each util/fixture)`.

#### Define a test which calls functions directly
Here is a simple example of a test which asserts a skill concept, and
then checks for its existence.

First, require
```
[jobtech-taxonomy-api.db.concept :as c]

```

Then write a test:
```
(test/deftest ^:concept-test-0 concept-test-0
  (test/testing "Test concept assertion."
    (c/assert-concept "skill" "cykla" "cykla")
    (let [found-concept (first (core/find-concept-by-preferred-term "cykla"))]
      (test/is (= "cykla" (get found-concept :preferred-label))))))
```

#### Define a test which calls the Luminus REST API
Here is a simple example of a test which asserts a skill concept, and
then checks for its existence via the REST API:

First, require
```
[jobtech-taxonomy-api.db.concept :as c]

```

Then write a test:
```
(test/deftest ^:changes-test-1 changes-test-1
  (test/testing "test event stream"
    (c/assert-concept "skill" "cykla" "cykla")
    (let [[status body] (util/send-request-to-json-service
                         :get "/v0/taxonomy/public/concepts"
                         :headers [util/header-auth-user]
                         :query-params [{:key "preferred-label", :val "cykla"}])]
      (test/is (= "cykla" (get (first body) :preferred-label))))))
```


### Local testing vs Jenkins testing
Kaocha can only use one of either the configuration to run locally, or to run from Jenkins. The default is Jenkins.

To run locally, check your project.clj that is has the right kaocha
resource commented:

```
    :project/kaocha {:dependencies [[lambdaisland/kaocha "0.0-418"]]
                    ;; You can only comment in one resource-path:
                    :resource-paths ["env/dev/resources"] ; comment in for local use
                    ; :resource-paths ["env/integration-test/resources"] ; comment in for Jenkins
                    }
```



## Logging

By default, logging functionality is provided by the
clojure.tools.logging library. The library provides macros that
delegate to a specific logging implementation. The default
implementation used in Luminus is the logback library.

Any Clojure data structures can be logged directly.


### Examples
```
(ns example
 (:require [clojure.tools.logging :as log]))

(log/info "Hello")
=>[2015-12-24 09:04:25,711][INFO][myapp.handler] Hello

(log/debug {:user {:id "Anonymous"}})
=>[2015-12-24 09:04:25,711][DEBUG][myapp.handler] {:user {:id "Anonymous"}}
```


### Description of log levels
#### trace
#### debug
#### info
#### warn
#### error
#### fatal

### Logging of exceptions


```
(ns example
 (:require [clojure.tools.logging :as log]))

(log/error (Exception. "I'm an error") "something bad happened")
=>[2015-12-24 09:43:47,193][ERROR][myapp.handler] something bad happened
  java.lang.Exception: I'm an error
    	at myapp.handler$init.invoke(handler.clj:21)
    	at myapp.core$start_http_server.invoke(core.clj:44)
    	at myapp.core$start_app.invoke(core.clj:61)
    	...
```

### Logging backends
### Configuring logging
Each profile has its own log configuration. For example, `dev`'s
configuration is located in `env/dev/resources/logback.xml`.

It works like a standard Java log configuration, with appenders and loggers.

The default configuration logs to standard out, and to log files in log/.

## License

EPL-2.0

Copyright © 2019 Jobtech

## CREATE dev-config for local developement
Create the file "dev-config.edn" with this content

```
;; WARNING
;; The dev-config.edn file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:dev true
 :port 3000
 ;; when :nrepl-port is set the application starts the nREPL server on load
 :nrepl-port 7000

 ; set your dev database connection URL here
 ; :database-url "datomic:free://localhost:4334/jobtech_taxonomy_api_dev"

 ; alternatively, you can use the datomic mem db for development:
 ; :database-url "datomic:mem://jobtech_taxonomy_api_datomic_dev"
}
```

## Running the tests in Docker

Sometimes it can be handy to be able to quickly checkout a branch from git and run all tests in a clean Docker environment.

Build the Docker image (the branch is configured at the top of the script):
   bin/test-docker.sh build

Run the tests (this requires that you have previously setup aws authentication: `~/.aws/accessKeys.csv`):
   bin/test-docker.sh


## COMMON ERRORS

If you get :server-type must be :cloud, :peer-server, or :local
you have forgot to start luminus. Run (start) in the user> namespace


# Contact Information

Bug reports are issued at [https://github.com/JobtechSwe/jobtech-taxonomy-api][the repo on Github].

Questions about the Taxonomy database, about Jobtech, about the API in
general are best emailed to [contact@jobtechdev.se][Jobtechdev contact
email adress].

Check out our other open APIs at [jobtechdev][Jobtechdev].


[0]: https://github.com/JobtechSwe/jobtech-taxonomy-database "The JobTech Taxonomy Database"
[1]: https://leiningen.org "Leiningen"
[2]: https://www.datomic.com "Datomic"
[3]: http://www.luminusweb.net "Luminus"
