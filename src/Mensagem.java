import java.io.Serializable;

public class Mensagem implements Serializable {

    String mensagem;

    public Mensagem(String mensagem, String opcao) {
        setMensagem(mensagem, opcao);
    }
    public Mensagem(String mensagem) {
        this.mensagem = new String(mensagem);
    }
    public String getMensagem() {
        return this.mensagem;
    }
    public void setMensagem(String fortune, String opcao) {
        String mensagem = "";

        switch (opcao) {
            case "1": {
                mensagem += "{\n" +
                        "\"method\": \"read\",\n" +
                        "\"args\": [\"\"]\n" +
                        "}";
                break;
            }
            case "2": {
                mensagem += "{\n" +
                        "\"method\": \"write\",\n" +
                        "\"args\": [\"" + fortune + "\"]\n" +
                        "}";
                break;
            }
        }
        this.mensagem = mensagem;
    }
}