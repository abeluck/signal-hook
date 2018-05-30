(ns signal-hook.signal.ISignalClient)

(defprotocol ISignalClient
  (is-ready? [_] "Is the client capable of sending messages?")
  (send-message! [_ body recipients] "Send the message body to the recipients, must be phone numbers")
  (send-group-message! [_ body groups] "Send the message body to the groups indicated by their ids"))

