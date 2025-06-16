/*
 * IMPLEMENTACAO DA SELECAO DE PEER, INDICADO POR: Item 2 (linhas 33-34)
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClienteRMI {

    public void iniciarCliente() {

        try {
            Registry registro = LocateRegistry.getRegistry("127.0.0.1", 1099);

            try (Scanner leitura = new Scanner(System.in)) {
                IMensagem stub = null;
                String escolhaNome = null;
                boolean conectou = false;

                while (!conectou) {
                    String[] nomesAtivos = registro.list();
                    if (nomesAtivos.length == 0) {
                        System.out.println("Nenhum peer ativo no momento. Tente novamente mais tarde.");
                        return;
                    }
                    System.out.println("Peers ativos disponíveis:");
                    for (int i = 0; i < nomesAtivos.length; i++) {
                        System.out.printf("  %d) %s%n", i + 1, nomesAtivos[i]);
                    }
                    //Item 4
                    System.out.print("Escolha o número do peer para conectar (ou 'r' para re-listar, 'x' para sair): ");
                    String entrada = leitura.nextLine().trim();
                    if (entrada.equalsIgnoreCase("x")) {
                        System.out.println("Saindo do cliente.");
                        return;
                    }
                    if (entrada.equalsIgnoreCase("r")) {
                        continue;
                    }
                    try {
                        int idx = Integer.parseInt(entrada);
                        if (idx < 1 || idx > nomesAtivos.length) {
                            System.out.println("Opção inválida. Deve ser número entre 1 e " + nomesAtivos.length);
                            continue;
                        }
                        escolhaNome = nomesAtivos[idx - 1];
                    } catch (NumberFormatException nfe) {
                        escolhaNome = entrada.trim();
                    }
                    try {
                        stub = (IMensagem) registro.lookup(escolhaNome);
                        conectou = true;
                    } catch (java.rmi.ConnectException e) {
                        System.out.println("Peer '" + escolhaNome + "' indisponível (ConnectException).");
                    } catch (java.rmi.NotBoundException e) {
                        System.out.println("Peer '" + escolhaNome + "' não encontrado (NotBoundException).");
                    } catch (Exception e) {
                        System.out.println("Erro ao conectar em '" + escolhaNome + "': " + e.getMessage());
                    }
                    if (!conectou) {
                        System.out.print("Não conseguiu conectar ao peer escolhido. Deseja tentar outro? (s/n): ");
                        String resp = leitura.nextLine();
                        if (!resp.equalsIgnoreCase("s")) {
                            return;
                        }
                    }
                }
                System.out.println("Conectado no peer: " + escolhaNome);
                String opcao;
                do {
                    System.out.println("\nEscolha uma opção:");
                    System.out.println("1) Read");
                    System.out.println("2) Write");
                    System.out.println("x) Exit");
                    System.out.print(">> ");
                    opcao = leitura.nextLine();
                    switch (opcao) {
                        case "1": {
                            Mensagem mensagem = new Mensagem("", opcao);
                            Mensagem resposta = stub.enviar(mensagem);
                            System.out.println("Resposta: " + resposta.getMensagem());
                            break;
                        }
                        case "2": {
                            System.out.print("Add fortune: ");
                            String fortune = leitura.nextLine();
                            Mensagem mensagem = new Mensagem(fortune, opcao);
                            Mensagem resposta = stub.enviar(mensagem);
                            System.out.println("Resposta: " + resposta.getMensagem());
                            break;
                        }
                        case "x": {
                            System.out.println("Encerrando cliente.");
                            System.exit(0);
                            break;
                        }
                        default: {
                            System.out.println("Opção inválida.");
                        }
                    }
                } while (!opcao.equals("x"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}