/*
 * IMPLEMENTACAO DO PEER 4, INDICADO POR: //Item 1 (linhas 24-28)
 */
public enum PeerLista {

    PEER1 {
        @Override
        public String getNome() {
            return "PEER1";
        }
    },
    PEER2 {
        public String getNome() {
            return "PEER2";
        }
    },
    PEER3 {
        public String getNome() {
            return "PEER3";
        }
    },
    //Item 1
    PEER4 {
        public String getNome() {
            return "PEER4";
        }
    };

    public String getNome() {
        return "NULO";
    }
}