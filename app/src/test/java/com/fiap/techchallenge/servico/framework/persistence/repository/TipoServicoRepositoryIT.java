package com.fiap.techchallenge.servico.framework.persistence.repository;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.servico.usecase.dto.TempoMedioServicoDTO;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class TipoServicoRepositoryIT {

    @Autowired
    private TipoServicoRepository tipoServicoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TipoServicoEntity tipoServico1;
    private TipoServicoEntity tipoServico2;
    private OrdemDeServicoEntity ordemDeServico;

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, 1, 1, 8, 0);

    @BeforeEach
    void setUp() {
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM oficina.servico").executeUpdate();
        entityManager.getEntityManager()
                .createNativeQuery("DELETE FROM oficina.tipo_servico").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        ordemDeServico = new OrdemDeServicoEntity();
        entityManager.persist(ordemDeServico);

        tipoServico1 = new TipoServicoEntity(null, "Alinhamento", new BigDecimal("150.00"));
        entityManager.persist(tipoServico1);

        tipoServico2 = new TipoServicoEntity(null, "Troca de Óleo", new BigDecimal("80.00"));
        entityManager.persist(tipoServico2);

        entityManager.flush();
    }

    // --- findTempoMedioPorTipoServico ---

    @Test
    void findTempoMedioPorTipoServico_deveRetornarTempoMedioCorretamente() {
        // tipoServico1 (Alinhamento): 60min e 120min → média 90min
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(60));
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(120));

        // tipoServico2 (Troca de Óleo): 30min → média 30min
        persistirServico(tipoServico2, BASE_TIME, BASE_TIME.plusMinutes(30));

        List<TempoMedioServicoDTO> resultado = tipoServicoRepository.findTempoMedioPorTipoServico();

        assertThat(resultado).hasSize(2);

        TempoMedioServicoDTO alinhamento = resultado.stream()
                .filter(r -> r.getNome().equals("Alinhamento"))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("90.00").compareTo(alinhamento.getTempoMedioMinutos()));

        TempoMedioServicoDTO trocaOleo = resultado.stream()
                .filter(r -> r.getNome().equals("Troca de Óleo"))
                .findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("30.00").compareTo(trocaOleo.getTempoMedioMinutos()));
    }

    @Test
    void findTempoMedioPorTipoServico_deveIgnorarServicosComDataNula() {
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(60));
        persistirServico(tipoServico1, null, BASE_TIME.plusMinutes(60)); // data_inicio nula
        persistirServico(tipoServico2, BASE_TIME, null);                // data_fim nula

        List<TempoMedioServicoDTO> resultado = tipoServicoRepository.findTempoMedioPorTipoServico();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Alinhamento");
    }

    @Test
    void findTempoMedioPorTipoServico_deveRetornarVazioSemServicosComDatas() {
        persistirServico(tipoServico1, null, null);

        List<TempoMedioServicoDTO> resultado = tipoServicoRepository.findTempoMedioPorTipoServico();

        assertThat(resultado).isEmpty();
    }

    @Test
    void findTempoMedioPorTipoServico_deveRetornarOrdenadoPorNome() {
        persistirServico(tipoServico2, BASE_TIME, BASE_TIME.plusMinutes(30)); // Troca de Óleo
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(60)); // Alinhamento

        List<TempoMedioServicoDTO> resultado = tipoServicoRepository.findTempoMedioPorTipoServico();

        assertThat(resultado)
                .extracting(TempoMedioServicoDTO::getNome)
                .isSortedAccordingTo(String::compareTo);
    }

    @Test
    void findTempoMedioPorTipoServico_deveRetornarVazioSemNenhumServico() {
        List<TempoMedioServicoDTO> resultado = tipoServicoRepository.findTempoMedioPorTipoServico();

        assertThat(resultado).isEmpty();
    }

    // --- findTempoMedioPorTipoServicoId ---

    @Test
    void findTempoMedioPorTipoServicoId_deveRetornarTempoMedioCorreto() {
        // tipoServico1: 60min e 120min → média 90min
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(60));
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(120));
        // tipoServico2 não deve influenciar
        persistirServico(tipoServico2, BASE_TIME, BASE_TIME.plusMinutes(999));

        Optional<TempoMedioServicoDTO> resultado =
                tipoServicoRepository.findTempoMedioPorTipoServicoId(tipoServico1.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Alinhamento");
        assertEquals(0, new BigDecimal("90.00").compareTo(resultado.get().getTempoMedioMinutos()));
    }

    @Test
    void findTempoMedioPorTipoServicoId_deveRetornarEmptyParaServicosComDataNula() {
        persistirServico(tipoServico1, null, null);

        Optional<TempoMedioServicoDTO> resultado =
                tipoServicoRepository.findTempoMedioPorTipoServicoId(tipoServico1.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void findTempoMedioPorTipoServicoId_deveRetornarEmptyParaIdInexistente() {
        Optional<TempoMedioServicoDTO> resultado =
                tipoServicoRepository.findTempoMedioPorTipoServicoId(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void findTempoMedioPorTipoServicoId_naoDeveConsiderarOutrosTipos() {
        persistirServico(tipoServico1, BASE_TIME, BASE_TIME.plusMinutes(60));
        persistirServico(tipoServico2, BASE_TIME, BASE_TIME.plusMinutes(999)); // não deve influenciar

        Optional<TempoMedioServicoDTO> resultado =
                tipoServicoRepository.findTempoMedioPorTipoServicoId(tipoServico1.getId());

        assertThat(resultado).isPresent();
        assertEquals(0, new BigDecimal("60.00").compareTo(resultado.get().getTempoMedioMinutos()));
    }

    // --- save e findById (comportamentos padrão JPA) ---

    @Test
    void save_deveGerarIdAutomaticamente() {
        TipoServicoEntity novo = new TipoServicoEntity(null, "Balanceamento", new BigDecimal("90.00"));
        TipoServicoEntity salvo = tipoServicoRepository.save(novo);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Balanceamento");
    }

    @Test
    void findById_deveRetornarEntidade() {
        entityManager.clear();

        TipoServicoEntity encontrado = tipoServicoRepository.findById(tipoServico1.getId()).orElseThrow();

        assertThat(encontrado.getNome()).isEqualTo("Alinhamento");
        assertThat(encontrado.getValor()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void delete_deveRemoverEntidade() {
        tipoServicoRepository.deleteById(tipoServico1.getId());
        entityManager.flush();

        Optional<TipoServicoEntity> resultado = tipoServicoRepository.findById(tipoServico1.getId());

        assertThat(resultado).isEmpty();
    }

    // --- helper ---

    private void persistirServico(TipoServicoEntity tipo, LocalDateTime inicio, LocalDateTime fim) {
        ServicoEntity servico = new ServicoEntity(tipo, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO);
        servico.setDataInicio(inicio);
        servico.setDataFim(fim);
        entityManager.persist(servico);
    }
}
