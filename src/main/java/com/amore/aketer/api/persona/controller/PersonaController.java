package com.amore.aketer.api.persona.controller;

import com.amore.aketer.api.persona.dto.PersonaTypeRowResponse;
import com.amore.aketer.service.PersonaTypeService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/persona-types")
public class PersonaController {

	private final PersonaTypeService personaTypeService;

	/**
	 * 페르소나 유형 목록 조회
	 */
	@GetMapping
	public Page<PersonaTypeRowResponse> list(
		@PageableDefault(size = 10, sort = "id", direction = DESC) Pageable pageable
	) {
		return personaTypeService.listPersonaTypes(pageable);
	}
}