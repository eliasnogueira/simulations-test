/*
 * MIT License
 *
 * Copyright (c) 2020 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package simulations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import client.SimulationsClient;
import data.SimulationDataFactory;
import exception.ConflictException;
import exception.NotFoundException;
import exception.UnprocessableEntityException;
import io.restassured.http.Headers;
import java.math.BigDecimal;
import model.Simulation;
import model.SimulationBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SimulationsTest {

    private static final String FAILED_VALIDATION = "data.SimulationDataProvider#failedValidations";

    private static SimulationsClient simulationsClient;
    private static SimulationDataFactory simulationDataFactory;

    @BeforeAll
    static void preCondition() {
        simulationsClient = new SimulationsClient();
        simulationDataFactory = new SimulationDataFactory();
    }

    @Test
    @DisplayName("Should return a simulation by the social security number")
    void shouldReturnSimulationBySocialSecurityNumber() {
        Simulation existingSimulation = simulationDataFactory.oneExistingSimulation();
        Simulation simulation = simulationsClient
            .getSimulationBySocialSecurityNumber(existingSimulation.getCpf());

        assertSoftly(softly -> {
            softly.assertThat(simulation.getName()).isEqualTo(existingSimulation.getName());
            softly.assertThat(simulation.getEmail()).isEqualTo(existingSimulation.getEmail());
            softly.assertThat(simulation.getAmount()).isEqualTo(existingSimulation.getAmount());
            softly.assertThat(simulation.getInstallments()).isEqualTo(existingSimulation.getInstallments());
            softly.assertThat(simulation.getInsurance()).isEqualTo(existingSimulation.getInsurance());
        });

    }

    @Test
    @DisplayName("Should create a new simulation")
    void shouldCreateNewSimulationSuccessfully() {
        Simulation simulation = new SimulationBuilder().
            name("Elias").cpf("98765432109").email("elias@elias.com").amount(new BigDecimal("30000.00")).
            installments(5).insurance(true).build();

        Headers headers = simulationsClient.createNewSimulation(simulation);
        assertThat(headers.getValue("Location")).contains(simulation.getCpf());

    }

    @Test
    @DisplayName("Should validate all existing simulations")
    void getAllExistingSimulations() {
        Simulation[] existingSimulations = simulationDataFactory.allExistingSimulations();
        Simulation[] simulationsRequested = simulationsClient.getAllSimulations();

        assertThat(simulationsRequested).containsExactlyInAnyOrder(existingSimulations);
    }

    @Test
    @DisplayName("Should filter by name a non-existing simulation")
    void simulationByNameNotFound() {
        NotFoundException notFoundException = simulationsClient
            .getSimulationByNameAndExpectNotFound("non-existent");
        assertThat(notFoundException.getMessage()).isEqualTo("Name not found");
    }

    @Test
    @DisplayName("Should find a simulation filtered by name")
    void returnSimulationByName() {
        Simulation existingSimulation = simulationDataFactory.oneExistingSimulation();
        Simulation[] simulationsByName = simulationsClient.getSimulationByName(existingSimulation.getName());

        assertThat(simulationsByName).contains(existingSimulation);
    }

    @Test
    @DisplayName("Should validate an CFP duplication")
    void simulationWithDuplicatedCpf() {
        Simulation existingSimulation = simulationDataFactory.oneExistingSimulation();
        ConflictException conflictException = simulationsClient
            .updateSimulationAndExpectConflictException(existingSimulation);

        assertThat(conflictException.getMessage()).isEqualTo("CPF already exists");
    }

    @Test
    @DisplayName("Should delete an existing simulation")
    void deleteSimulationSuccessfully() {
        Simulation existingSimulation = simulationDataFactory.oneExistingSimulation();
        simulationsClient.deleteSimulation(existingSimulation.getCpf());
    }

    @Test
    @DisplayName("Should validate the return when a non-existent simulation is sent")
    void notFoundWhenDeleteSimulation() {
        String nonExistentCPF = simulationDataFactory.notExistentCpf();
        NotFoundException notFoundException = simulationsClient.deleteSimulationAndReturnNotFound(nonExistentCPF);

        assertThat(notFoundException.getMessage()).isEqualTo(String.format("CPF %s not found", nonExistentCPF));
    }

    @Test
    @DisplayName("Should update an existing simulation")
    void changeSimulationSuccessfully() {
        Simulation existingSimulation = simulationDataFactory.oneExistingSimulation();

        Simulation newSimulation = simulationDataFactory.validSimulation();
        newSimulation.setCpf(existingSimulation.getCpf());
        newSimulation.setInsurance(existingSimulation.getInsurance());

        Simulation simulationReturned = simulationsClient.updateSimulation(existingSimulation.getCpf(), newSimulation);

        assertThat(simulationReturned).isEqualTo(newSimulation);
    }

    @Test
    @DisplayName("Should validate the return of an update for a non-existent CPF")
    void changeSimulationCpfNotFound() {
        String nonExistentCpf = simulationDataFactory.notExistentCpf();
        Simulation newSimulation = simulationDataFactory.validSimulation();

        NotFoundException notFoundException = simulationsClient
            .updateSimulationAndExpectNotFoundException(nonExistentCpf, newSimulation);
        assertThat(notFoundException.getMessage()).isEqualTo(String.format("CPF %s not found", nonExistentCpf));
    }

    @ParameterizedTest(name = "Scenario: {2}")
    @MethodSource(value = FAILED_VALIDATION)
    @DisplayName("Should validate all the invalid scenarios")
    void invalidSimulations(Simulation invalidSimulation, String path, String validationMessage) {
        UnprocessableEntityException unprocessableEntityException = simulationsClient
            .updateSimulationAndExpectUnprocessableEntityException(invalidSimulation);

        assertThat(unprocessableEntityException.getErrors()).containsValue(validationMessage);
    }

}
