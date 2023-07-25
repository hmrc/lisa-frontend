# Lifetime ISA

This service provides the ability for ISA managers to apply for approval to become a Lifetime ISA provider.

## Requirements

This service is written in [Scala 2.13](http://www.scala-lang.org/) and [Play 2.8](http://playframework.com/), and needs at least Java 11 to run.

## Authentication

This customer logs into this service using [GOV.UK Verify](https://www.gov.uk/government/publications/introducing-govuk-verify/introducing-govuk-verify).

## Running Locally

1. **[Install Service-Manager](https://github.com/hmrc/service-manager/wiki/Install#install-service-manager)**
2. `git clone git@github.com:hmrc/lisa-frontend.git`
3. `sbt "run 8884"`
4. `sm2 --start LISA_FRONTEND_ALL`

The unit tests can be run by running
```
sbt test
```

To run a single unit test
```
sbt testOnly SPEC_NAME
```

To run all tests
```
./run_all_tests.sh
```

## User Journeys

See the LISA User Journeys documentation on Confluence for help with these.

## URL

`http://localhost:8884/lifetime-isa`

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
