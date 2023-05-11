package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Set;

public class ContaService {

    private final ConnectionFactory connection;

    public ContaService() {
        this.connection = new ConnectionFactory();
    }


    public Set<Conta> listarContasAbertas() {
        Connection conn = connection.recuperarConexao();
        return new ContaDAO(conn).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta != null) return conta.getSaldo();
        throw new RegraDeNegocioException("Conta inválida");
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        if (!conta.getEstaAtiva()) {
            throw new RegraDeNegocioException("Conta não está ativa");
        }

        Connection conn = connection.recuperarConexao();
        BigDecimal valorAtual = conta.getSaldo().subtract(valor);
        new ContaDAO(conn).alterarSaldo(conta.getNumero(), valorAtual);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }
        if (!conta.getEstaAtiva()) {
            throw new RegraDeNegocioException("Conta não está ativa");
        }
        Connection conn = connection.recuperarConexao();
        BigDecimal valorAtual = conta.getSaldo().add(valor);
        new ContaDAO(conn).alterarSaldo(conta.getNumero(), valorAtual);
    }

    public void encerrarLogico(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).deletarLogico(conta.getNumero());
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperarConexao();
        new ContaDAO(conn).deletar(conta.getNumero());
    }

    public void realizarTransferencia(Integer numeroDaContaOrigem, Integer numeroDaContaDestino, BigDecimal valor) {
        Conta contaO = buscarContaPorNumero(numeroDaContaOrigem);
        Conta contaD = buscarContaPorNumero(numeroDaContaDestino);

        if (contaO != null && contaD != null) {
            this.realizarSaque(numeroDaContaOrigem, valor);
            this.realizarDeposito(numeroDaContaDestino, valor);
        } else {
            throw new RegraDeNegocioException("Uma ou mais conta informada, não foram localizadas.");
        }

    }

    private Conta buscarContaPorNumero(Integer numero) {
        Connection conn = connection.recuperarConexao();
        return new ContaDAO(conn).listarPorNumero(numero);

        //return contas
        //        .stream()
        //        .filter(c -> c.getNumero() == numero)
        //        .findFirst()
        //        .orElseThrow(() -> new RegraDeNegocioException("Não existe conta cadastrada com esse número!"));
    }
}
