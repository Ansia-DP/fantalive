package it.fantalive.standings_service;

import org.springframework.boot.SpringApplication;

public class TestStandingsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(StandingsServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
