package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private final Connection conn;

    ContaDAO(Connection connection) {
        this.conn = connection;
    }

    public void salvar(DadosAberturaConta dadosDaConta) {
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email)" +
                " VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = this.conn.prepareStatement(sql);

            preparedStatement.setInt(1, conta.getNumero());
            preparedStatement.setBigDecimal(2, BigDecimal.ZERO);
            preparedStatement.setString(3, cliente.getNome());
            preparedStatement.setString(4, cliente.getCpf());
            preparedStatement.setString(5, cliente.getEmail());

            preparedStatement.execute();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Conta> listar() {
        Set<Conta> contas = new HashSet<>();
        String sql = "SELECT * FROM conta";

        try {
            PreparedStatement ps = this.conn.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                contas.add(this.criarObjeto(resultSet));
            }
            resultSet.close();
            ps.close();
            conn.close();
            return contas;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Conta listarPorNumero(Integer numero) {
        String sql = "SELECT * FROM conta WHERE numero = ?";

        PreparedStatement ps;
        ResultSet rs;
        Conta conta = null;

        try {
            ps = this.conn.prepareStatement(sql);
            ps.setInt(1, numero);
            rs = ps.executeQuery();

            while (rs.next()) {
                conta = criarObjeto(rs);
            }
            rs.close();
            ps.close();
            conn.close();
            return conta;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void alterarSaldo(Integer numero, BigDecimal valor) {
        PreparedStatement ps;
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ?";

        try {
            ps = this.conn.prepareStatement(sql);
            ps.setBigDecimal(1, valor);
            ps.setInt(2, numero);

            ps.execute();

            ps.close();
            conn.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deletar(Integer numero) {
        PreparedStatement ps;
        String sql = "DELETE FROM conta WHERE numero = ?";

        try {
            ps = this.conn.prepareStatement(sql);
            ps.setInt(1, numero);

            ps.execute();

            ps.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Conta criarObjeto(ResultSet rs) {
        try {
            Integer numeroRecuperado = rs.getInt(1);
            BigDecimal saldo = rs.getBigDecimal(2);
            String nome = rs.getString(3);
            String cpf = rs.getString(4);
            String email = rs.getString(5);

            DadosCadastroCliente dadosCadastroCliente =
                    new DadosCadastroCliente(nome, cpf, email);
            Cliente cliente = new Cliente(dadosCadastroCliente);

            return new Conta(numeroRecuperado, saldo, cliente);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
