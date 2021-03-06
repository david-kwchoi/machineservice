package com.practice.machineservice;


import com.practice.machineservice.dto.Machines;
import com.practice.machineservice.model.Machine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.practice.machineservice.utils.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class MachineServiceIntegrationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testCreateMachineFollowedByQueriesByNameAndIdAndAll() {
        ResponseEntity<Machine> createResp = restTemplate.postForEntity("/machine/add", UNSAVED_MACHINE, Machine.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).isNotNull();

        ResponseEntity<Machines> respByName = restTemplate.getForEntity("/machines/" + UNSAVED_MACHINE.getName(), Machines.class);
        assertThat(respByName.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(respByName.getBody()).isNotNull();
        assertThat(respByName.getBody().getMachines()).isNotEmpty();
        List<Machine> machines = respByName.getBody().getMachines();

        assertThat(machines.size()).isEqualTo(1);
        assertThat(machines.get(0).getName()).isEqualTo(UNSAVED_MACHINE.getName());
        assertThat(machines.get(0).getDescription()).isEqualTo(UNSAVED_MACHINE.getDescription());
        assertThat(machines.get(0).getThroughputMins()).isEqualTo(UNSAVED_MACHINE.getThroughputMins());

        ResponseEntity<Machine> respById = restTemplate.getForEntity("/machine/" + createResp.getBody().getId(), Machine.class);
        assertThat(respById.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respById.getBody()).isNotNull();
        assertThat(respById.getBody().getName()).isEqualTo(createResp.getBody().getName());
        assertThat(respById.getBody().getDescription()).isEqualTo(createResp.getBody().getDescription());
        assertThat(respById.getBody().getThroughputMins()).isEqualTo(createResp.getBody().getThroughputMins());

        ResponseEntity<Machines> respGetAll = restTemplate.getForEntity("/machines", Machines.class);
        assertThat(respGetAll.getStatusCode()).isEqualTo(HttpStatus.OK);

        Machines result = respGetAll.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getMachines()).isNotNull();
        assertThat(result.getMachines().size()).isGreaterThan(0);
        assertThat(result.getMachines().stream().map(Machine::getId).collect(Collectors.toList())).contains(createResp.getBody().getId());
    }

    @Test
    public void testUpsert() {
        restTemplate.put("/machine/99", UNSAVED_MACHINE_4);
        restTemplate.put("/machine/99", UNSAVED_MACHINE_4);
        restTemplate.put("/machine/99", UNSAVED_MACHINE_4);

        ResponseEntity<Machines> getAllResp = restTemplate.getForEntity("/machines/" + UNSAVED_MACHINE_4.getName(), Machines.class);
        assertThat(getAllResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResp.getBody()).isNotNull();

        Machines machines = getAllResp.getBody();

        assertThat(machines.getMachines()).isNotNull();
        assertThat(machines.getMachines().size()).isEqualTo(3);
        machines.getMachines().forEach(m -> {
                    assertThat(m.getName()).isEqualTo(UNSAVED_MACHINE_4.getName());
                    assertThat(m.getDescription()).isEqualTo(UNSAVED_MACHINE_4.getDescription());
                    assertThat(m.getThroughputMins()).isEqualTo(UNSAVED_MACHINE_4.getThroughputMins());
                }
        );

        Machine machine1 = machines.getMachines().get(0);
        machine1.setThroughputMins(99);
        restTemplate.put("/machine/" + machine1.getId(), machine1);
        ResponseEntity<Machine> getByIdResp = restTemplate.getForEntity("/machine/" + machine1.getId(), Machine.class);
        assertThat(getByIdResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResp.getBody()).isNotNull();
        assertThat(getByIdResp.getBody().getThroughputMins()).isEqualTo(machine1.getThroughputMins());
        assertThat(getByIdResp.getBody().getDescription()).isEqualTo(machine1.getDescription());
        assertThat(getByIdResp.getBody().getName()).isEqualTo(machine1.getName());
    }

    @Test
    public void testRepeatedCreate() {
        ResponseEntity<Machine> createResp = restTemplate.postForEntity("/machine/add", UNSAVED_MACHINE_3, Machine.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).isNotNull();

        ResponseEntity<Machine> createResp2 = restTemplate.postForEntity("/machine/add", UNSAVED_MACHINE_3, Machine.class);
        assertThat(createResp2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp2.getBody()).isNotNull();

        ResponseEntity<Machine> createResp3 = restTemplate.postForEntity("/machine/add", UNSAVED_MACHINE_3, Machine.class);
        assertThat(createResp3.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp3.getBody()).isNotNull();

        ResponseEntity<Machines> getAllResp = restTemplate.getForEntity("/machines/" + UNSAVED_MACHINE_3.getName(), Machines.class);
        assertThat(getAllResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResp.getBody()).isNotNull();

        Machines machines = getAllResp.getBody();

        assertThat(machines.getMachines()).isNotNull();
        assertThat(machines.getMachines().size()).isEqualTo(3);
        machines.getMachines().forEach(m -> {
                    assertThat(m.getName()).isEqualTo(UNSAVED_MACHINE_3.getName());
                    assertThat(m.getDescription()).isEqualTo(UNSAVED_MACHINE_3.getDescription());
                    assertThat(m.getThroughputMins()).isEqualTo(UNSAVED_MACHINE_3.getThroughputMins());
                }
        );
    }

    @Test
    public void testUpdatesForCreated() {
        ResponseEntity<Machine> createResp = restTemplate.postForEntity("/machine/add", UNSAVED_MACHINE_2, Machine.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).isNotNull();

        ResponseEntity<Machine> respById = restTemplate.getForEntity("/machine/" + createResp.getBody().getId(), Machine.class);
        assertThat(respById.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respById.getBody()).isNotNull();
        assertThat(respById.getBody().getName()).isEqualTo(createResp.getBody().getName());
        assertThat(respById.getBody().getDescription()).isEqualTo(createResp.getBody().getDescription());
        assertThat(respById.getBody().getThroughputMins()).isEqualTo(createResp.getBody().getThroughputMins());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(UPDATE_MACHINE_DESC_JSON, headers);

        ResponseEntity<Machine> updatedResp = restTemplate.postForEntity("/machine/" + createResp.getBody().getId(), request, Machine.class);

        assertThat(updatedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedResp.getBody()).isNotNull();

        Machine updatedMachine = updatedResp.getBody();
        assertThat(updatedMachine.getId()).isEqualTo(createResp.getBody().getId());
        assertThat(updatedMachine.getName()).isEqualTo(createResp.getBody().getName());
        assertThat(updatedMachine.getDescription()).isEqualTo(UPDATE_DESC_MACHINE.getDescription());
        assertThat(updatedMachine.getThroughputMins()).isEqualTo(createResp.getBody().getThroughputMins());

        HttpEntity<String> invalidRequest = new HttpEntity<>(INVALID_NAME_MACHINE_JSON, headers);

        ResponseEntity<String> invalidResult = restTemplate.postForEntity("/machine/" + createResp.getBody().getId(), invalidRequest, String.class);
        assertThat(invalidResult.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidResult.getBody()).contains("name must be less than/equal to 80 characters");

        HttpEntity<String> invalidThroughputRequest = new HttpEntity<>(INVALID_THROUGHPUT_MACHINE_JSON, headers);

        ResponseEntity<String> invalidThroughputResult = restTemplate.postForEntity("/machine/" + createResp.getBody().getId(), invalidThroughputRequest, String.class);
        assertThat(invalidThroughputResult.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidThroughputResult.getBody()).contains("throughput per minute must be a positive integer");
    }
}
