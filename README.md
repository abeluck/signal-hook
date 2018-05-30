# signal-hook

> Send Signal messages with an HTTP POST

**WARNING: This project is under development. It doesn't do what you think it
does. Don't use it.**

![](signal-hook.png)

signal-hook provides a small (some might even say *micro*) service for sending messages to individuals and
groups on [Signal][signal] with [twilio-like payloads][twiliosms] via HTTP.

signal-hook uses [signal-cli][signal-cli] under the hood.

## Prerequisites

**WARNING: This project is under development. It doesn't do what you think it
does. Don't use it.**

This project uses Clojure and you will need [Leiningen][lein] 2.0 or above
installed to proceed. You'll also need JDK 8 or higher. If Docker is your
thing, you can use that too.

## Usage

**WARNING: This project is under development. It doesn't do what you think it
does. Don't use it.**

POST some JSON. Get a Message.

httpie:

    http -a admin:changeme -v --json POST localhost:3000/send To:='"+15558675309"' Body="Hi J"

curl:

    curl -X POST -d '{"Body": "Hi J", "To": "+15558675309"}' -H "Content-Type: application/json" http://admin:changeme@localhost:3000/send

Auth: The authentication story in signal-hook is pretty basic at the moment. You can
supply multiple username password pairs, which are authenticated using http
basic-auth. All authed users can send messages.


HTTPS: **You should run signal-hook behind a HTTPS/TLS proxy**. I recommend
fronting it with nginx + letsencrypt.

### Quickstart

Building is pretty easy if you have lein installed:

    lein uberjar

This will create the self-contained jar file `target/uberjar/signal-hook.jar`.

You can execute it with java, like so:

        java -jar target/uberjar/signal-hook.jar -Dconf=prod-config.edn

However you will need some configuration to make the whole thing work, that's the `prod-config.edn` bit.

Copy the sample file `prod-config.sample.edn` and edit it, supplying your own values.

Or, you can inject the configuration with environment variables:

```
SIGNAL_CLI__PATH=/local/path/to/signal-cli
SIGNAL_CLI__STATE_DIR=/home/<your username>/.config/signal
SIGNAL_CLI__USERNAME=+611234561234
API_CREDS='{:admin "changeme"}' 
```

You'll need to install [signal-cli][signal-cli] yourself manually and tweak the config as appropriate.

### Docker. Docker. Docker.

#### Docker build

Pretty simple:

    lein uberjar
    docker build -t signal-hook .

#### Docker run

1. Create a persistent docker volume to store signal-cli's state

        sudo docker volume create v-signal-hook

2. Start the container

        sudo docker run -d --name signaltest -p 3000:3000 \
                        -e API_CREDS='{:admin "changeme"}' \
                        -e SIGNAL_CLI__USERNAME='"+1234567890"' 
                        -v v-signal-hook:/var/lib/signal-cli \
                        signal-hook

    Substitute in the phone number that will be sending (aka the From party) the messages. Also substitute in the user name and password you want to protect the service: `{:admin "changeme"}`.

3. Link signal-hook to your existing Signal number

        sudo docker exec -it signaltest \
               /usr/local/bin/signal-cli --config /var/lib/signal-cli link --name signal-test-device

    Change `signal-test-device` to any other name, it will show up in Signal under "Linked Devices"


### API Format

The expected webhook payload is similar to [twilio's send messages
api][twiliosms].

To send an outgoing message, make an HTTP POST to the `https://your.signal-webhook.url/send` endpoint.

| Parameter | Description                                                                                                                       | Example                             |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| To        | The recipient's phone number or group id, or an array of multiple numbers and group ids. Group ids must be prefixed with `group:` | `[ "+15558675309", "group:XXX==" ]` |
| Body      | The text of the message you want to send. Maximum size XXXX bytes.                                                                | Hello World!                        |
| MediaUrl  | A URL to image to send as an attachment                                                                                           | https://example.com/someimage.jpg   |


<!-- table made with https://www.tablesgenerator.com/markdown_tables -->

The `To` parameter is **required** in your POST to send the message. And **at
least one** of `Body` or `MediaUrl` is required.

#### Examples of valid payloads:

```json
{
    "Body": "Send message to a single group", 
    "To": "group:aaaaaaaaa=="
}
```

```json
{
    "Body": "Send message to a single individual", 
    "To": "+341234567890"
}
```

```json
{
    "Body": "Send message to multiple mixed recipients", 
    "To": ["+4912345678", "group:aaaaaaaaa=="]
}
```

## Development

To start a web server for the application, run:

    lein run 

To run tests, refreshing automatically

    lein test-refresh

## License

Copyright © 2018 Abel Luck <abel@guardianproject.info>

Distributed under the AGPL v3 license. See [LICENSE.md](LICENSE.md) for more information.

Project generated using Luminus version "2.9.12.42"


[signal]: https://signal.org
[twiliosms]: https://www.twilio.com/docs/sms/send-messages
[signal-cli]: https://github.com/AsamK/signal-cli
[lein]: https://leiningen.org/

