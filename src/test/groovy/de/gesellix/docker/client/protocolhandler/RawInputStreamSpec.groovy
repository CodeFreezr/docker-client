package de.gesellix.docker.client.protocolhandler

import org.apache.commons.io.IOUtils
import spock.lang.Specification

import static de.gesellix.docker.client.protocolhandler.StreamType.STDOUT

class RawInputStreamSpec extends Specification {

  def "should stream the complete payload"() {
    given:
    def actualText = "docker\nrocks!\n\n"
    def headerAndPayload = new RawHeaderAndPayload(STDOUT, actualText.bytes)
    def inputStream = new ByteArrayInputStream((byte[]) headerAndPayload.bytes)
    def outputStream = new ByteArrayOutputStream()

    when:
    def copied = IOUtils.copy(new RawInputStream(inputStream), outputStream)

    then:
    copied == actualText.size()
    and:
    outputStream.toString() == actualText
  }

  def "should stream the complete payload with empty final frame"() {
    given:
    def actualText = "docker\nstill\nrocks!"
    def headerAndPayload1 = new RawHeaderAndPayload(STDOUT, actualText.bytes)
    def headerAndPayload2 = new RawHeaderAndPayload(STDOUT, new byte[0])
    def bytes = headerAndPayload1.bytes + headerAndPayload2.bytes
    def inputStream = new ByteArrayInputStream((byte[]) bytes)
    def outputStream = new ByteArrayOutputStream()

    when:
    def copied = IOUtils.copy(new RawInputStream(inputStream), outputStream)

    then:
    copied == actualText.size()
    and:
    outputStream.toString() == actualText
  }

  static class RawHeaderAndPayload {

    private StreamType streamType
    private byte[] payload

    RawHeaderAndPayload(streamType, payload) {
      this.streamType = streamType
      this.payload = payload
    }

    def getBytes() {
      def bytes = [
          streamType.streamTypeId,
          0, 0, 0,
          // assume a short payload
          0, 0, 0, payload.length
      ]
      payload.each {
        bytes << it
      }
      return bytes
    }
  }
}
