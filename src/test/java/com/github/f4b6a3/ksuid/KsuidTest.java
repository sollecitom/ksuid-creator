package com.github.f4b6a3.ksuid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.github.f4b6a3.ksuid.Ksuid;

public class KsuidTest {

	private static final int DEFAULT_LOOP_MAX = 1_000;

	@Test
	public void testSegmentIoExample() {

		// REPRESENTATION:
		// String: 0ujtsYcgvSTl8PAuAdqWYSMnLOv
		// Raw: 0669F7EFB5A1CD34B5F99D1154FB6853345C9735
		//
		// COMPONENTS:
		//
		// Time: 2017-10-09 21:00:47 -0700 PDT
		// Timestamp: 107608047
		// Payload: B5A1CD34B5F99D1154FB6853345C9735
		//
		// URL: https://github.com/segmentio/ksuid

		String string = "0ujtsYcgvSTl8PAuAdqWYSMnLOv";
		String raw = "0669F7EFB5A1CD34B5F99D1154FB6853345C9735";
		Instant instant = Instant.parse("2017-10-10T04:00:47Z"); // UTC
		long timestamp = 107608047; // KSUID time
		String payload = "B5A1CD34B5F99D1154FB6853345C9735";

		// instantiate a KSUID from bytes
		byte[] bytes = new BigInteger(raw, 16).toByteArray();
		Ksuid ksuid = Ksuid.from(bytes);

		// String: 0ujtsYcgvSTl8PAuAdqWYSMnLOv
		assertEquals(string, ksuid.toString());

		// Raw: 0669F7EFB5A1CD34B5F99D1154FB6853345C9735
		String raw2 = new BigInteger(1, ksuid.toBytes()).toString(16).toUpperCase();
		assertEquals(raw.replaceAll("^0+", ""), raw2);

		// Time: 2017-10-09 21:00:47 -0700 PDT
		assertEquals(instant, ksuid.getInstant());

		// Timestamp: 107608047
		assertEquals(Ksuid.toUnixTime(timestamp), ksuid.getTime());

		// Payload: B5A1CD34B5F99D1154FB6853345C9735
		String payload2 = new BigInteger(1, ksuid.getPayload()).toString(16).toUpperCase();
		assertEquals(payload, payload2);
	}

	@Test
	public void testConstructorTimeAndRandom() {

		Random random = new Random();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {

			// get a random time and a random payload
			long time = random.nextLong() & 0x00000000ffffffffL;
			byte[] payload = new byte[Ksuid.PAYLOAD_BYTES];
			random.nextBytes(payload);

			// build the expected byte array
			byte[] expectedBytes = new byte[Ksuid.KSUID_BYTES];
			ByteBuffer buffer = ByteBuffer.allocate(Ksuid.KSUID_BYTES + 4);
			buffer.putLong(time - Ksuid.EPOCH_OFFSET); // apply offset
			buffer.put(payload);
			System.arraycopy(buffer.array(), 4, expectedBytes, 0, Ksuid.KSUID_BYTES);

			// get the actual byte array
			Ksuid ksuid = new Ksuid(time, payload);
			byte[] actualBytes = ksuid.toBytes();

			// compare the actual byte array to the expected byte array
			for (int j = 0; j < Ksuid.KSUID_BYTES; j++) {
				assertEquals(expectedBytes[j], actualBytes[j]);
			}
		}

		try {
			long time = 0x0000000000000000L;
			byte[] payload = null;
			new Ksuid(time, payload);
			fail("Should throw an exception");
		} catch (IllegalArgumentException e) {
			// success
		}

		try {
			long time = 0x0000000000000000L;
			byte[] payload = new byte[Ksuid.PAYLOAD_BYTES + 1];
			new Ksuid(time, payload);
			fail("Should throw an exception");
		} catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	public void testToString() {
		Random random = new Random();
		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			byte[] bytes = new byte[Ksuid.KSUID_BYTES];
			random.nextBytes(bytes);
			Ksuid ksuid = Ksuid.from(bytes);
			assertEquals(toBase62(bytes), ksuid.toString());
		}
	}

	@Test
	public void testFromString() {
		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			String string = getRandomString();
			Ksuid ksuid = Ksuid.from(string);
			assertEquals(Arrays.toString(fromBase62(string)), Arrays.toString(ksuid.toBytes()));
		}
	}

	@Test
	public void testToAndFromString() {
		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			String string = getRandomString();
			Ksuid ksuid2 = Ksuid.from(string);
			assertEquals(string, ksuid2.toString());
		}
	}

	@Test
	public void testToAndFromBytes() {

		Random random = new Random();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {

			byte[] expectedBytes = new byte[Ksuid.KSUID_BYTES];
			random.nextBytes(expectedBytes);

			Ksuid ksuid = Ksuid.from(expectedBytes);
			byte[] actualBytes = ksuid.toBytes();

			for (int j = 0; j < Ksuid.KSUID_BYTES; j++) {
				assertEquals(expectedBytes[j], actualBytes[j]);
			}
		}

		try {
			byte[] bytes = null;
			Ksuid.from(bytes);
			fail("Should throw an exception");
		} catch (IllegalArgumentException e) {
			// success
		}

		try {
			byte[] bytes = new byte[Ksuid.KSUID_BYTES + 1];
			Ksuid.from(bytes);
			fail("Should throw an exception");
		} catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	public void testGetTimeAndPayload() {

		Random random = new Random();

		for (int i = 0; i < 100; i++) {

			long time = (random.nextLong() & 0x00000000ffffffffL) + Ksuid.EPOCH_OFFSET;
			byte[] payload = new byte[Ksuid.PAYLOAD_BYTES];
			random.nextBytes(payload);

			// Instance methods
			Ksuid ksuid = new Ksuid(time, payload);
			assertEquals(time, ksuid.getTime());
			assertEquals(Instant.ofEpochSecond(time), ksuid.getInstant());
			for (int j = 0; j < payload.length; j++) {
				assertEquals(payload[j], ksuid.getPayload()[j]);
			}

			// Static methods
			String string = new Ksuid(time, payload).toString();
			assertEquals(time, Ksuid.getTime(string));
			assertEquals(Instant.ofEpochSecond(time), Ksuid.getInstant(string));
			for (int j = 0; j < payload.length; j++) {
				assertEquals(payload[j], Ksuid.getPayload(string)[j]);
			}
		}
	}

	@Test
	public void testIsValidString() {

		String ksuid = null; // Null
		assertFalse("Null KSUID should be invalid.", Ksuid.isValid(ksuid));

		ksuid = ""; // length: 0
		assertFalse("KSUID with empty string should be invalid .", Ksuid.isValid(ksuid));

		ksuid = "0123456789ABCDEFGHIJKLMNOPQ"; // All upper case
		assertTrue("KSUID in upper case should valid.", Ksuid.isValid(ksuid));

		ksuid = "0123456789abcdefghijklmnopq"; // All lower case
		assertTrue("KSUID in lower case should be valid.", Ksuid.isValid(ksuid));

		ksuid = "0123456789ABCDEFGHIJKLMNOP"; // length: 26
		assertFalse("KSUID length lower than 27 should be invalid.", Ksuid.isValid(ksuid));

		ksuid = "0123456789ABCDEFGHIJKLMNOPQR"; // length: 28
		assertFalse("KSUID length greater than 27 should be invalid.", Ksuid.isValid(ksuid));

		ksuid = "#0123456789ABCDEFGHIJKLMNOP"; // Special char
		assertFalse("KSUID with special chars should be invalid. ", Ksuid.isValid(ksuid));
	}

	@Test
	public void testEquals() {
		Random random = new Random();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			byte[] bytes = new byte[Ksuid.KSUID_BYTES];
			random.nextBytes(bytes);
			Ksuid ksuid1 = Ksuid.from(bytes);
			Ksuid ksuid2 = new Ksuid(ksuid1);
			assertEquals(ksuid1, ksuid2);
		}
	}

	@Test
	public void testCompareTo() {
		Random random = new Random();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			byte[] bytes = new byte[Ksuid.KSUID_BYTES];

			random.nextBytes(bytes);
			Ksuid ksuid1 = Ksuid.from(bytes);
			BigInteger number1 = new BigInteger(1, bytes);

			random.nextBytes(bytes);
			Ksuid ksuid2 = Ksuid.from(bytes);
			Ksuid ksuid3 = Ksuid.from(bytes);
			BigInteger number2 = new BigInteger(1, bytes);
			BigInteger number3 = new BigInteger(1, bytes);

			assertEquals(number1.compareTo(number2), ksuid1.compareTo(ksuid2));
			assertEquals(number2.compareTo(number3), ksuid2.compareTo(ksuid3));
		}
	}

	@Test
	public void testMinAndMaxKsuidString() {

		byte[] minBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] maxBytes = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

		String minString = "000000000000000000000000000";
		String maxString = "aWgEPTl1tmebfsQzFP4bxwgy80V";

		Ksuid minKsuid = new Ksuid(minBytes);
		Ksuid maxKsuid = new Ksuid(maxBytes);

		assertEquals(minString, minKsuid.toString());
		assertEquals(maxString, maxKsuid.toString());

		// overflow exception
		String overflow = 'z' + maxString.substring(1);
		try {
			Ksuid.from(overflow);
			fail("Should throw overflow exception");
		} catch (IllegalArgumentException e) {
			// success!
		}
	}

	@Test
	public void testMinAndMaxKsuidInstant() {

		byte[] minBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		byte[] maxBytes = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

		Instant minInstant = Instant.parse("2014-05-13T16:53:20Z"); // 0x00000000 + 1_400_000_000L
		Instant maxInstant = Instant.parse("2150-06-19T23:21:35Z"); // 0xffffffff + 1_400_000_000L

		Ksuid minKsuid = new Ksuid(minBytes);
		Ksuid maxKsuid = new Ksuid(maxBytes);

		assertEquals(minInstant, minKsuid.getInstant());
		assertEquals(maxInstant, maxKsuid.getInstant());
	}

	@Test
	public void testGetInstant() {

		Instant instant = Instant.parse("2106-02-07T06:28:16Z"); // 0x00000000
		byte[] payload = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

		long seconds0 = 0x00000000L;
		Ksuid ksuid0 = new Ksuid(seconds0, payload);
		assertEquals(instant, ksuid0.getInstant());

		long seconds1 = 0x00000000L - 1; // 2106-02-07T06:28:16Z - 1
		Ksuid ksuid1 = new Ksuid(seconds1, payload);
		assertEquals(instant.minusSeconds(1), ksuid1.getInstant());

		long seconds2 = 0x00000000L + 1; // 2106-02-07T06:28:16Z + 1
		Ksuid ksuid2 = new Ksuid(seconds2, payload);
		assertEquals(instant.plusSeconds(1), ksuid2.getInstant());

		long seconds3 = 0xffffffffL; // 2106-02-07T06:28:16Z
		Ksuid ksuid3 = new Ksuid(seconds3, payload);
		assertEquals(instant.minusSeconds(1), ksuid3.getInstant());

		long seconds4 = 0xffffffffL - 1; // 2106-02-07T06:28:16Z - 1 - 1
		Ksuid ksuid4 = new Ksuid(seconds4, payload);
		assertEquals(instant.minusSeconds(2), ksuid4.getInstant());

		long seconds5 = 0xffffffffL + 1; // 2106-02-07T06:28:16Z - 1 + 1
		Ksuid ksuid5 = new Ksuid(seconds5, payload);
		assertEquals(instant, ksuid5.getInstant());
	}

	@Test
	public void testGetInstantRollover() {

		Instant minInstant = Instant.parse("2014-05-13T16:53:20Z"); // 0x00000000 + 1_400_000_000L
		Instant maxInstant = Instant.parse("2150-06-19T23:21:35Z"); // 0xffffffff + 1_400_000_000L
		byte[] payload = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };

		long seconds0 = Ksuid.EPOCH_OFFSET + 0x00000000L; // 2014-05-13T16:53:20Z
		Ksuid ksuid0 = new Ksuid(seconds0, payload);
		assertEquals(minInstant, ksuid0.getInstant());

		long seconds1 = Ksuid.EPOCH_OFFSET + 0xffffffffL; // 2150-06-19T23:21:35Z
		Ksuid ksuid1 = new Ksuid(seconds1, payload);
		assertEquals(maxInstant, ksuid1.getInstant());

		long seconds2 = Ksuid.EPOCH_OFFSET + (0xffffffffL + 1); // 2014-05-13T16:53:20Z (ROLLOVER)
		Ksuid ksuid2 = new Ksuid(seconds2, payload);
		assertEquals(minInstant, ksuid2.getInstant());

		long seconds3 = Ksuid.EPOCH_OFFSET + (0x00000000L - 1); // 2150-06-19T23:21:35Z (ROLLOVER BACKWARDS)
		Ksuid ksuid3 = new Ksuid(seconds3, payload);
		assertEquals(maxInstant, ksuid3.getInstant());
	}

	@Test
	public void testRemainder() {
		Random random = new Random();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			byte[] bytes = new byte[20];
			random.nextBytes(bytes);
			int divisor = random.nextInt() & 0x7fffffff; // positive divisor

			BigInteger number1 = new BigInteger(1, bytes);
			BigInteger quotient1 = number1.divide(BigInteger.valueOf(divisor));
			BigInteger reminder1 = number1.remainder(BigInteger.valueOf(divisor));

			int[] number2 = Ksuid.toInts(bytes);
			int[] quotient2 = new int[5];
			int remainder2 = Ksuid.remainder(number2, divisor, quotient2);

			assertEquals(number1, new BigInteger(1, Ksuid.fromInts(number2)));
			assertEquals(quotient1, new BigInteger(1, Ksuid.fromInts(quotient2)));
			assertEquals(reminder1.intValue(), remainder2);
		}
	}

	@Test
	public void testMultiply() {
		Random random = new Random();

		for (int i = 0; i < DEFAULT_LOOP_MAX; i++) {
			byte[] bytes = new byte[20];
			random.nextBytes(bytes);
			int multiplier = random.nextInt() & 0x7fffffff; // positive
			int addend = random.nextInt() & 0x7fffffff; // positive

			BigInteger number1 = new BigInteger(1, bytes);
			BigInteger product1 = number1.multiply(BigInteger.valueOf(multiplier)).add(BigInteger.valueOf(addend));

			// truncate BigInteger product!
			byte[] temp0 = product1.toByteArray();
			byte[] temp1 = new byte[20];
			int t0 = temp0.length;
			int t1 = temp1.length;
			while (t0 > 0 && t1 > 0) {
				temp1[--t1] = temp0[--t0];
			}
			byte[] productBytes1 = temp1;

			int[] number2 = Ksuid.toInts(bytes);
			int[] product2 = Ksuid.multiply(number2, multiplier, addend, false);
			byte[] productBytes2 = Ksuid.fromInts(product2);

			assertEquals(number1, new BigInteger(1, Ksuid.fromInts(number2)));
			assertEquals(Arrays.toString(productBytes1), Arrays.toString(productBytes2));
		}
	}

	private String getRandomString() {

		Random random = new Random();
		char[] chars = new char[Ksuid.KSUID_CHARS];

		chars[0] = '0'; // zero to avoid overflow
		for (int i = 1; i < Ksuid.KSUID_CHARS; i++) {
			chars[i] = Ksuid.BASE62_ALPHABET[random.nextInt(Ksuid.BASE62_RADIX)];
		}

		return new String(chars);
	}

	private byte[] fromBase62(String string) {

		char[] chars = string.toCharArray();
		BigInteger number = BigInteger.ZERO;

		BigInteger n = BigInteger.valueOf(62);

		for (int c : chars) {
			final long value = Ksuid.BASE62_MAP[c];
			number = n.multiply(number).add(BigInteger.valueOf(value));
		}

		// prepare a byte buffer
		byte[] result = number.toByteArray();
		byte[] buffer = new byte[20];
		int r = result.length; // result index
		int b = buffer.length; // buffer index

		// fill in the byte buffer
		while (b > 0 && r > 0) {
			buffer[--b] = result[--r];
		}

		return buffer;
	}

	private String toBase62(byte[] bytes) {

		// it must be a POSITIVE big number
		BigInteger number = new BigInteger(1, bytes);

		char[] buffer = new char[27];
		int b = buffer.length; // buffer index

		BigInteger n = BigInteger.valueOf(62);

		// fill in the buffer backwards using remainder operation
		while (number.compareTo(BigInteger.ZERO) > 0) {
			buffer[--b] = Ksuid.BASE62_ALPHABET[number.remainder(n).intValue()];
			number = number.divide(n);
		}

		// add padding left
		while (b > 0) {
			buffer[--b] = '0';
		}

		return new String(buffer);
	}
}
