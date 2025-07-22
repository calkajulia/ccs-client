# ccs client

Simple test client for the CCS server. Discovers server via UDP broadcast and sends 3 random arithmetic operations via TCP.

## Usage

```bash
# Compile and run
javac Main.java
java Main

# When prompted, enter command:
client <port>
```

*After running `java Main`, the program waits for you to type the command on the console.*