package com.fiap.techchallenge.servico.framework.persistence.repository;

import com.fiap.techchallenge.os.domain.OrdemDeServico;
import com.fiap.techchallenge.os.framework.persistence.OrdemDeServicoEntity;
import com.fiap.techchallenge.servico.domain.ServicoStatus;
import com.fiap.techchallenge.servico.framework.persistence.entity.ServicoEntity;
import com.fiap.techchallenge.servico.framework.persistence.entity.TipoServicoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ServicoRepositoryIT {

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private OrdemDeServicoEntity ordemDeServico;
    private TipoServicoEntity tipoServico1;
    private TipoServicoEntity tipoServico2;
    private ServicoEntity servico1;
    private ServicoEntity servico2;

    @BeforeEach
    void setUp() {
        ordemDeServico = new OrdemDeServicoEntity();
        entityManager.persist(ordemDeServico);

        tipoServico1 = TipoServicoEntity.builder()
                .nome("Troca de Óleo")
                .valor(new BigDecimal("150.00"))
                .build();
        entityManager.persist(tipoServico1);

        tipoServico2 = TipoServicoEntity.builder()
                .nome("Alinhamento")
                .valor(new BigDecimal("100.00"))
                .build();
        entityManager.persist(tipoServico2);

        servico1 = new ServicoEntity(tipoServico1, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO);
        entityManager.persist(servico1);

        servico2 = new ServicoEntity(tipoServico2, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO);
        entityManager.persist(servico2);

        entityManager.flush();
    }

    // --- findAllByOsId ---

    @Test
    void findAllByOsId_deveRetornarServicosCorretos() {
        List<ServicoEntity> resultado = servicoRepository.findAllByOsId(ordemDeServico.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(ServicoEntity::getId)
                .containsExactlyInAnyOrder(servico1.getId(), servico2.getId());
    }

    @Test
    void findAllByOsId_deveRetornarVazioParaOsSemServicos() {
        OrdemDeServicoEntity osSemServicos = new OrdemDeServicoEntity();
        entityManager.persistAndFlush(osSemServicos);

        List<ServicoEntity> resultado = servicoRepository.findAllByOsId(osSemServicos.getId());

        assertThat(resultado).isEmpty();
    }

    @Test
    void findAllByOsId_deveRetornarVazioParaIdInexistente() {
        List<ServicoEntity> resultado = servicoRepository.findAllByOsId(999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void findAllByOsId_naoDeveRetornarServicosDeOutraOs() {
        OrdemDeServicoEntity outraOs = new OrdemDeServicoEntity();
        entityManager.persist(outraOs);

        ServicoEntity servicoOutraOs = new ServicoEntity(tipoServico1, outraOs, ServicoStatus.AGUARDANDO_INICIO);
        entityManager.persistAndFlush(servicoOutraOs);

        List<ServicoEntity> resultado = servicoRepository.findAllByOsId(ordemDeServico.getId());

        assertThat(resultado)
                .extracting(ServicoEntity::getId)
                .doesNotContain(servicoOutraOs.getId());
    }

    // --- existsByTipoServicoId ---

    @Test
    void existsByTipoServicoId_deveRetornarTrueQuandoExiste() {
        boolean resultado = servicoRepository.existsByTipoServicoId(tipoServico1.getId());

        assertThat(resultado).isTrue();
    }

    @Test
    void existsByTipoServicoId_deveRetornarFalseParaIdInexistente() {
        boolean resultado = servicoRepository.existsByTipoServicoId(999L);

        assertThat(resultado).isFalse();
    }

    @Test
    void existsByTipoServicoId_deveRetornarFalseAposDeleteDoServico() {
        OrdemDeServicoEntity outraOs = new OrdemDeServicoEntity();
        entityManager.persist(outraOs);

        TipoServicoEntity tipoSemServico = TipoServicoEntity.builder()
                .nome("Balanceamento")
                .valor(new BigDecimal("80.00"))
                .build();
        entityManager.persistAndFlush(tipoSemServico);

        boolean resultado = servicoRepository.existsByTipoServicoId(tipoSemServico.getId());

        assertThat(resultado).isFalse();
    }

    // --- save e findById (comportamentos padrão JPA) ---

    @Test
    void save_deveGerarIdAutomaticamente() {
        ServicoEntity novo = new ServicoEntity(tipoServico1, ordemDeServico, ServicoStatus.AGUARDANDO_INICIO);
        ServicoEntity salvo = servicoRepository.save(novo);

        assertThat(salvo.getId()).isNotNull();
    }

    @Test
    void findById_deveRetornarServicoCorreto() {
        entityManager.clear();

        ServicoEntity encontrado = servicoRepository.findById(servico1.getId()).orElseThrow();

        assertThat(encontrado.getStatus()).isEqualTo(ServicoStatus.AGUARDANDO_INICIO);
        assertThat(encontrado.getTipoServico().getNome()).isEqualTo("Troca de Óleo");
    }
}
