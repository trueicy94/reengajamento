package br.com.reengajamento.acompanhamento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(
		basePackages =
				"br.com.reengajamento.acompanhamento.client"
)
@SpringBootApplication
public class AcompanhamentoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcompanhamentoServiceApplication.class, args);
	}

}