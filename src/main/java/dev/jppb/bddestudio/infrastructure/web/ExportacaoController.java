package dev.jppb.bddestudio.infrastructure.web;

import dev.jppb.bddestudio.application.service.ArquivoFeature;
import dev.jppb.bddestudio.application.service.ExportacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExportacaoController {

    private final ExportacaoService service;

    @GetMapping("/historias/{id}/feature")
    public ResponseEntity<String> exportar(@PathVariable Long id) {
        ArquivoFeature arquivo = service.exportarFeature(id);
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nome()).build().toString())
                .body(arquivo.conteudo());
    }
}
