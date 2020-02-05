# Lifetime ISA

[Build Status](https://build.tax.service.gov.uk/job/LISA/job/lisa-frontend/) [ ![Download](https://api.bintray.com/packages/hmrc/releases/lisa-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/lisa-frontend/_latestVersion)

This service provides the ability for ISA managers to apply for approval to become a Lifetime ISA provider.

## Requirements

This service is written in [Scala 2.11](http://www.scala-lang.org/) and [Play 2.6](http://playframework.com/), so needs at least a [JRE 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) to run.

## Authentication

This customer logs into this service using [GOV.UK Verify](https://www.gov.uk/government/publications/introducing-govuk-verify/introducing-govuk-verify).

## Running Locally

1. **[Install Service-Manager](https://github.com/hmrc/service-manager/wiki/Install#install-service-manager)**
2. `git clone git@github.com:hmrc/lisa-frontend.git`
3. `sbt "run 8884"`
4. `sm --start LISA_FRONTEND_ALL -f`

The unit tests can be run by running
```
sbt test
```

To run a single unit test
```
sbt testOnly SPEC_NAME
```

## User Journeys

See the LISA User Journeys documentation on Confluence for help with these.

## URL

`http://localhost:8884/lifetime-isa`

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

