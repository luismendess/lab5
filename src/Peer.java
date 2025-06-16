/*
 * IMPLEMETACAO DA INTERFACE PARA EXIBIR PEERS ATIVOS, INDICADO POR: Item 2 (linhas 88-90)
 * IMPLEMENTACAO DO UNBIND, INDICADO POR: Item 3 (linhas 133-135)
 */

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class Peer implements IMensagem {

    private Registry servidorRegistro;
    private String meuNomePeer;

    public Peer() {
    }

    @Override
    public Mensagem enviar(Mensagem mensagem) throws RemoteException {
        Mensagem resposta;
        try {
            System.out.println("Mensagem recebida: " + mensagem.getMensagem());
            resposta = new Mensagem(parserJSON(mensagem.getMensagem()));
        } catch (Exception e) {
            e.printStackTrace();
            resposta = new Mensagem("{\n\"result\": false\n}");
        }
        return resposta;
    }

    public String parserJSON(String json) {
        String result = "false";
        String fortune = "-1";
        String[] v = json.split(":");

        if (v.length < 2) {
            return "{\n\"result\": false\n}";
        }
        String[] v1 = v[1].split("\"");
        if (v1.length < 2) {
            return "{\n\"result\": false\n}";
        }
        String method = v1[1];
        if (method.equals("write")) {

            String[] p = json.split("\\[");
            if (p.length >= 2) {
                String[] p1 = p[1].split("]");
                if (p1.length >= 1) {
                    String[] p2 = p1[0].split("\"");
                    if (p2.length >= 2) {
                        fortune = p2[1];
                        Principal pv2 = new Principal();
                        pv2.write(fortune);
                    }
                }
            }
        } else if (method.equals("read")) {
            // Read file
            Principal pv2 = new Principal();
            fortune = pv2.read();
        }

        result = "{\n\"result\": \"" + fortune + "\"\n}";
        System.out.println("Resposta gerada: " + result);
        return result;
    }

    public void iniciar() {
        try {

            List<PeerLista> listaPeers = new ArrayList<>();
            for (PeerLista p : PeerLista.values()) {
                listaPeers.add(p);
            }
            try {
                this.servidorRegistro = LocateRegistry.createRegistry(1099);
                System.out.println("RMI Registry criado na porta 1099.");
            } catch (java.rmi.server.ExportException e) {
                System.out.println("Registro já iniciado. Usando o existente.");
                this.servidorRegistro = LocateRegistry.getRegistry();
            }
            // Item 2
            String[] listaAlocados = this.servidorRegistro.list();
            System.out.println("Peers atualmente ativos no Registry:");
            for (String nome : listaAlocados) {
                System.out.println("  " + nome);
            }
            SecureRandom sr = new SecureRandom();
            PeerLista escolhido = null;
            int tentativas = 0;
            boolean repetido = true;
            boolean cheio = false;

            while (repetido && !cheio) {
                repetido = false;
                PeerLista candidato = listaPeers.get(sr.nextInt(listaPeers.size()));

                for (String nomeAtivo : listaAlocados) {
                    if (nomeAtivo.equals(candidato.getNome())) {
                        System.out.println(candidato.getNome() + " já ativo. Tentando outro...");
                        repetido = true;
                        tentativas++;
                        break;
                    }
                }
                if (!repetido) {
                    escolhido = candidato;
                }
                if (listaAlocados.length > 0 && tentativas >= listaPeers.size()) {
                    cheio = true;
                }
            }

            if (cheio || escolhido == null) {
                System.out.println("Sistema cheio ou sem peer disponível. Tente mais tarde.");
                System.exit(1);
            }

            this.meuNomePeer = escolhido.getNome();
            IMensagem stub = (IMensagem) UnicastRemoteObject.exportObject(this, 0);
            // Faz rebind no registry com o nome escolhido
            this.servidorRegistro.rebind(this.meuNomePeer, stub);
            System.out.println(this.meuNomePeer + " registrado no RMI Registry, aguardando conexões...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    // Item 3
                    UnicastRemoteObject.unexportObject(this, true);
                    this.servidorRegistro.unbind(meuNomePeer);
                    System.out.println(meuNomePeer + " desregistrado.");
                } catch (Exception ex) {
                    System.err.println("Erro ao desconectar peer: " + ex.getMessage());
                }
            }));

            new ClienteRMI().iniciarCliente();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Peer servidor = new Peer();
        servidor.iniciar();
    }
}