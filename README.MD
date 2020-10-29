# Simulations test
This test project belongs to a set of projects listed bellow:

* [simulations-client](https://github.com/eliasnogueira/simulations-client)
* [test-parent](https://github.com/eliasnogueira/test-parent)

## What does this project do?
This is the test project for the [credit-simulator-api](https://github.com/eliasnogueira/credit-simulator-api), so you need to run locally the API service to be able to run the tests.

The [SimulationsTest](https://github.com/eliasnogueira/simulations-test/blob/master/src/test/java/simulations/SimulationsTest.java) 
class uses the restrictions-client project as a dependency to hit the correct endpoints.
You can configure the credit-restrictions-api (if you changed it) in the [api.properties](https://github.com/eliasnogueira/simulations-test/blob/master/src/test/resources/api.properties) file.

The package `src/main/java/data` contains the data classes:
* [CfpGenerator](https://github.com/eliasnogueira/simulations-test/blob/master/src/main/java/data/CpfGenerator.java): class to generate a new valid CPF
* [SimulationDataFactory](https://github.com/eliasnogueira/simulations-test/blob/master/src/main/java/data/SimulationDataFactory.java): factory class for fake data creation
* [SimulationsDataProvider](https://github.com/eliasnogueira/simulations-test/blob/master/src/main/java/data/SimulationDataProvider.java): data provider for the negative scenarios