package pt.lsts.imc4j.generator;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.xml.bind.JAXB;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.AnnotationSpec.Builder;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.FormatConversion;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;
import pt.lsts.imc4j.xml.FieldType;
import pt.lsts.imc4j.xml.MessageType;
import pt.lsts.imc4j.xml.Messages;

public class IMCGenerator {

	private static final String pkgMsgs = "pt.lsts.imc4j.msg";
	private static final String pkgDefs = "pt.lsts.imc4j.def";
	private static final String outDir = "src";
	
	static LinkedHashMap<String, TypeSpec> bitfields = null;
	static LinkedHashMap<String, TypeSpec> enums = null;
	static LinkedHashMap<String, TypeSpec> groups = null;

	static void initialize(Messages proto) {
		if (bitfields == null)
			bitfields = parseBitfields(proto);

		if (enums == null)
			enums = parseEnums(proto);

		if (groups == null)
			groups = parseSupertypes(proto);
	}

	static void parseFieldEnums(TypeSpec.Builder parent, List<FieldType> fields) {

		fields.forEach(f -> {
			if (!f.getFieldValue().isEmpty()) {
				// add inner class
				TypeSpec.Builder defBuilder = TypeSpec.enumBuilder(f.getAbbrev().toUpperCase());
				defBuilder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

				f.getFieldValue().forEach(b -> {
					defBuilder.addEnumConstant(f.getPrefix() + "_" + b.getAbbrev(),
							TypeSpec.anonymousClassBuilder("$Ll", b.getId()).build());
				});

				defBuilder.addField(TypeName.LONG, "value", Modifier.PROTECTED);

				defBuilder.addMethod(MethodSpec.methodBuilder("value").returns(TypeName.LONG)
						.addStatement("return $N", "value").build());

				defBuilder.addMethod(MethodSpec.constructorBuilder().addParameter(TypeName.LONG, "value")
						.addStatement("this.$N = $N", "value", "value").build());

				MethodSpec.Builder valOf = MethodSpec.methodBuilder("valueOf")
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC).addParameter(TypeName.LONG, "value")
						.addException(IllegalArgumentException.class)
						.returns(ClassName.bestGuess(f.getAbbrev().toUpperCase()));
				valOf.beginControlFlow("for ($L v : $L.values())", f.getAbbrev().toUpperCase(),
						f.getAbbrev().toUpperCase());
				valOf.beginControlFlow("if (v.value == value)");
				valOf.addStatement("return v");
				valOf.endControlFlow();
				valOf.endControlFlow();
				valOf.addStatement("throw new IllegalArgumentException(\"Invalid value for $L: \"+value)",
						f.getAbbrev().toUpperCase());

				defBuilder.addMethod(valOf.build());

				parent.addType(defBuilder.build());
			}
		});
	}

//	static TypeSpec parseState(Messages proto) {
//		TypeSpec.Builder state = TypeSpec.classBuilder(ClassName.get(pkgMsgs, "IMCState"));
//		state.addModifiers(Modifier.PUBLIC);
//		
//		proto.getMessage().forEach(m -> {
//			state.addMethod(MethodSpec.methodBuilder(m.getAbbrev())
//					.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//					.returns(ParameterizedTypeName.get(ClassName.get(IMCQuery.class),
//							ClassName.bestGuess(m.getAbbrev())))
//					.addStatement("return $T.q($L.class)", IMCQuery.class, m.getAbbrev()).build());
//		});
//
//		return state.build();
//	}
//	
	static TypeSpec parseFactory(Messages proto) {
		TypeSpec.Builder factory = TypeSpec.classBuilder(ClassName.get(pkgMsgs, "MessageFactory"));
		factory.addModifiers(Modifier.PUBLIC);

		proto.getMessage().forEach(m -> {
			factory.addField(FieldSpec
					.builder(TypeName.INT, "ID_" + m.getAbbrev(), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer("$L", m.getId().intValue()).build());
		});

		MethodSpec.Builder createId = MethodSpec.methodBuilder("create").addParameter(TypeName.INT, "mgid")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(ClassName.get(pkgMsgs, "Message"));

		createId.beginControlFlow("switch(mgid)");

		proto.getMessage().forEach(m -> {
			String name = m.getAbbrev();
			createId.beginControlFlow("case $L:", "ID_" + name);
			createId.addStatement("return new $L()", name);
			createId.endControlFlow();
		});
		createId.beginControlFlow("default:");
		createId.addStatement("return null");
		createId.endControlFlow();
		createId.endControlFlow();
		factory.addMethod(createId.build());
		
		MethodSpec.Builder createAbbrev = MethodSpec.methodBuilder("create").addParameter(String.class, "abbrev")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(ClassName.get(pkgMsgs, "Message"));
		createAbbrev.addStatement("return create(idOf(abbrev))");
		factory.addMethod(createAbbrev.build());
		
		MethodSpec.Builder idOf = MethodSpec.methodBuilder("idOf").addParameter(String.class, "abbrev")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(TypeName.INT);

		idOf.beginControlFlow("switch(abbrev)");

		proto.getMessage().forEach(m -> {
			String name = m.getAbbrev();
			idOf.beginControlFlow("case $S:", name);
			idOf.addStatement("return $L", "ID_"+name);
			idOf.endControlFlow();
		});
		idOf.beginControlFlow("default:");
		idOf.addStatement("return -1");
		idOf.endControlFlow();
		idOf.endControlFlow();
		factory.addMethod(idOf.build());
		
		WildcardTypeName wtn = WildcardTypeName.subtypeOf(ClassName.get("pt.lsts.imc4j.msg", "Message"));
		ParameterizedTypeName ptn = ParameterizedTypeName.get(ClassName.get(Class.class), wtn);
		
		MethodSpec.Builder classOf = MethodSpec.methodBuilder("classOf").addParameter(Integer.class, "mgid")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(ptn);

		classOf.beginControlFlow("switch(mgid)");

		proto.getMessage().forEach(m -> {
			String name = m.getAbbrev();
			classOf.beginControlFlow("case $L:", "ID_"+name);
			classOf.addStatement("return $L", name+".class");
			classOf.endControlFlow();
		});
		classOf.beginControlFlow("default:");
		classOf.addStatement("return Message.class");
		classOf.endControlFlow();
		classOf.endControlFlow();
		factory.addMethod(classOf.build());
		
		MethodSpec.Builder classOfString = MethodSpec.methodBuilder("classOf").addParameter(String.class, "abbrev")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(ptn);

		classOfString.addStatement("return classOf(idOf(abbrev))");
		factory.addMethod(classOfString.build());
		
		return factory.build();
	}

	static TypeSpec parseHeader(Messages proto) {
		TypeSpec.Builder msgBuilder = TypeSpec.classBuilder(ClassName.get(pkgMsgs, "Message"));
		msgBuilder.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);

		parseFieldEnums(msgBuilder, proto.getHeader().getField());

		MethodSpec size = MethodSpec.methodBuilder("size").addModifiers(Modifier.PUBLIC).returns(TypeName.INT)
				.addCode("return serializeFields().length;\n").build();
		msgBuilder.addMethod(size);

		MethodSpec mgid = MethodSpec.methodBuilder("mgid").addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
				.addJavadoc("The identification number of the message").returns(TypeName.INT).build();
		msgBuilder.addMethod(mgid);
		
		MethodSpec abbrev = MethodSpec.methodBuilder("abbrev").addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
				.addJavadoc("The name (abbreviation) of the message").returns(String.class).build();
		msgBuilder.addMethod(abbrev);
		
		msgBuilder.addMethod(MethodSpec.methodBuilder("toString").addModifiers(Modifier.PUBLIC, Modifier.FINAL).returns(String.class)
				.addStatement("return $T.asJson(this)", FormatConversion.class).build());

		MethodSpec serFields = MethodSpec.methodBuilder("serializeFields")
				.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC).addJavadoc("Serialize this message's payload")
				.returns(ArrayTypeName.of(TypeName.BYTE)).build();
		msgBuilder.addMethod(serFields);
		
		MethodSpec deserFields = MethodSpec.methodBuilder("deserializeFields")
				.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC).addJavadoc("Deserialize this message's payload")
				.addException(IOException.class)
				.addParameter(ByteBuffer.class, "buf").build();
		msgBuilder.addMethod(deserFields);

		MethodSpec serialize = MethodSpec.methodBuilder("serialize").addModifiers(Modifier.PUBLIC)
				.addJavadoc("Serialize this message").returns(ArrayTypeName.of(TypeName.BYTE))
				.addStatement("return $T.serializeMessage(this)", SerializationUtils.class).build();
		msgBuilder.addMethod(serialize);
		
		MethodSpec deserialize = MethodSpec.methodBuilder("deserialize").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addJavadoc("Read a message from a byte array")
				.addException(Exception.class)
				.addParameter(byte[].class, "data")
				.returns(ClassName.get(pkgMsgs, "Message"))
				.addStatement("return $T.deserializeMessage(data)", SerializationUtils.class).build();
		msgBuilder.addMethod(deserialize);

		for (FieldType f : proto.getHeader().getField()) {
			FieldSpec spec;

			switch (f.getAbbrev()) {
			case "sync":
				spec = parseField("Message", f, f.getValue());
				spec = spec.toBuilder().addModifiers(Modifier.FINAL).build();

				msgBuilder.addField(FieldSpec.builder(ClassName.SHORT, "SYNC_WORD")
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.initializer("(short)$L", f.getValue()).build());

				break;
			case "timestamp":
				spec = parseField("Message", f, "System.currentTimeMillis() / 1000.0");
				spec = spec.toBuilder().build();
				break;
			case "src_ent":
			case "dst_ent":
				spec = parseField("Message", f, "0xFF");
				spec = spec.toBuilder().build();
				break;
			case "src":
			case "dst":
				spec = parseField("Message", f, "0xFFFF");
				spec = spec.toBuilder().build();
				break;
			default:
				continue;
			}
			msgBuilder.addField(spec);
		}

		return msgBuilder.build();
	}

	static String cleanJavadoc(String doc) {
		String txt = "";
		for (String line : doc.split("\n")) {
			if (!line.isEmpty())
				txt += line.trim() + "\n";
		}
		return txt.trim() + "\n";
	}

	static TypeSpec parseMessage(MessageType mtype, Messages proto) {

		initialize(proto);

		TypeSpec.Builder builder = TypeSpec.classBuilder(ClassName.get(pkgMsgs, mtype.getAbbrev()));
		builder.addModifiers(Modifier.PUBLIC);
		if (mtype.getDescription() != null)
			builder.addJavadoc(cleanJavadoc(mtype.getDescription().getValue()));
		builder.addField(FieldSpec.builder(TypeName.INT, "ID_STATIC", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
				.initializer("$L", mtype.getId()).build());
		
		builder.addMethod(MethodSpec.methodBuilder("abbrev").addModifiers(Modifier.PUBLIC).returns(String.class)
				.addCode("return $S;\n", mtype.getAbbrev()).build());

		builder.addMethod(MethodSpec.methodBuilder("mgid").addModifiers(Modifier.PUBLIC).returns(TypeName.INT)
				.addCode("return $L;\n", mtype.getId()).build());

		builder.addMethod(serializeMethod(mtype));
		builder.addMethod(deserializeMethod(mtype));

		if (groups.containsKey(mtype.getAbbrev()))
			builder.superclass(ClassName.get(pkgMsgs, groups.get(mtype.getAbbrev()).name));
		else
			builder.superclass(ClassName.get(pkgMsgs, "Message"));

		parseFieldEnums(builder, mtype.getField());
		mtype.getField().forEach(f -> builder.addField(parseField(mtype.getAbbrev(), f, "0")));

		return builder.build();
	}

	static MethodSpec serializeMethod(MessageType mtype) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("serializeFields").addModifiers(Modifier.PUBLIC)
				.returns(ArrayTypeName.of(TypeName.BYTE));

		if (mtype.getField().isEmpty()) {
			method.addCode("return new byte[0];\n");
			return method.build();
		}

		method.beginControlFlow("try");
		method.addStatement("$T _data = new $T()", ByteArrayOutputStream.class, ByteArrayOutputStream.class);
		method.addStatement("$T _out = new $T(_data)", DataOutputStream.class, DataOutputStream.class);

		mtype.getField().forEach(ftype -> {
			String value = ftype.getAbbrev();
			if (ftype.getUnit() != null && ftype.getUnit().equals("Enumerated")) {
				value = String.format("(%s != null? %s.value() : 0)", value, value);
				if (!ftype.getType().endsWith("64_t"))
					value = "(int)" + value;
			} else if (ftype.getUnit() != null && ftype.getUnit().equals("Bitfield")) {
				String sum = "_" + value;
				String iterator = "__" + value;
				String type = value.toUpperCase();
				if (ftype.getBitfieldDef() != null && !ftype.getBitfieldDef().isEmpty())
					type = ftype.getBitfieldDef();

				method.addStatement("long $L = 0", sum);
				method.beginControlFlow("if ($L != null)", value);
				method.beginControlFlow("for ($L $L : $L.toArray(new $L[0]))", type, iterator, value, type);
				method.addStatement("$L += $L.value()", sum, iterator);
				method.endControlFlow();
				method.endControlFlow();
				value = sum;

				if (!ftype.getType().endsWith("64_t"))
					value = "(int)" + value;
			}

			switch (ftype.getType()) {
			case "uint8_t":
			case "int8_t":
				method.addStatement("_out.writeByte($L)", value);
				break;
			case "uint16_t":
			case "int16_t":
				method.addStatement("_out.writeShort($L)", value);
				break;
			case "int32_t":
			case "uint32_t":
				method.addStatement("_out.writeInt((int)$L)", value);
				break;
			case "int64_t":
			case "uint64_t":
				method.addStatement("_out.writeLong($L)", value);
				break;
			case "fp32_t":
				method.addStatement("_out.writeFloat($L)", value);
				break;
			case "fp64_t":
				method.addStatement("_out.writeDouble($L)", value);
				break;
			case "rawdata":
				method.addStatement("$T.serializeRawdata(_out, $L)", SerializationUtils.class, value);
				break;
			case "plaintext":
				if ("TupleList".equals(ftype.getUnit()))
					method.addStatement("$T.serializePlaintext(_out, $L == null? null : $L.toString())",
							SerializationUtils.class, value, value);				
				else
					method.addStatement("$T.serializePlaintext(_out, $L)", SerializationUtils.class, value);
				break;
			case "message":
				method.addStatement("$T.serializeInlineMsg(_out, $L)", SerializationUtils.class, value);
				break;
			case "message-list":
				method.addStatement("$T.serializeMsgList(_out, $L)", SerializationUtils.class, value);
				break;
			default:
				break;
			}
		});

		method.addStatement("return _data.toByteArray()");
		method.endControlFlow();

		method.beginControlFlow("catch ($T e)", IOException.class);
		method.addStatement("e.printStackTrace()");
		method.addStatement("return new byte[0]");
		method.endControlFlow();

		return method.build();
	}

	static MethodSpec deserializeMethod(MessageType mtype) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("deserializeFields").addModifiers(Modifier.PUBLIC)
				.addParameter(ByteBuffer.class, "buf").addException(IOException.class);

		method.beginControlFlow("try");
		for (FieldType ftype : mtype.getField()) {
			boolean enumerated = (ftype.getUnit() != null && ftype.getUnit().equals("Enumerated"));
			boolean bitfield = (ftype.getUnit() != null && ftype.getUnit().equals("Bitfield"));

			String retrieval = "null";
			switch (ftype.getType()) {
			case "uint8_t":
				retrieval = "buf.get() & 0xFF";
				break;
			case "int8_t":
				retrieval = "buf.get()";
				break;
			case "uint16_t":
				retrieval = "buf.getShort() & 0xFFFF";
				break;
			case "int16_t":
				retrieval = "buf.getShort()";
				break;
			case "uint32_t":
				retrieval = "buf.getInt() & 0xFFFFFFFF";
				break;
			case "int32_t":
				retrieval = "buf.getInt()";
				break;
			case "int64_t":
			case "uint64_t":
				retrieval = "buf.getLong()";
				break;
			case "fp32_t":
				retrieval = "buf.getFloat()";
				break;
			case "fp64_t":
				retrieval = "buf.getDouble()";
				break;
			case "rawdata":
				retrieval = "SerializationUtils.deserializeRawdata(buf)";
				break;
			case "plaintext":
				if ("TupleList".equals(ftype.getUnit())) {
					retrieval = "new TupleList(SerializationUtils.deserializePlaintext(buf))";
				}
				else
					retrieval = "SerializationUtils.deserializePlaintext(buf)";
				break;
			case "message":
				retrieval = "SerializationUtils.deserializeInlineMsg(buf)";
				break;
			case "message-list":
					retrieval = "SerializationUtils.deserializeMsgList(buf)";
				break;
			default:
				break;
			}

			if (enumerated) {
				String enumName = ftype.getEnumDef() != null && !ftype.getEnumDef().isEmpty() ? ftype.getEnumDef()
						: ftype.getAbbrev().toUpperCase();
				method.addStatement("$L = $L.valueOf($L)", ftype.getAbbrev(), enumName, retrieval);
			} else if (bitfield) {
				String bitfieldName = ftype.getBitfieldDef() != null && !ftype.getBitfieldDef().isEmpty()
						? ftype.getBitfieldDef() : ftype.getAbbrev().toUpperCase();
				// pc.flags.clear();
				method.addStatement("long $L = $L", ftype.getAbbrev() + "_val", retrieval);
				method.addStatement("$L.clear()", ftype.getAbbrev());
				method.beginControlFlow("for ($L $L : $L.values())", bitfieldName, bitfieldName + "_op", bitfieldName);
				method.beginControlFlow("if (($L & $L.value()) == $L.value())", ftype.getAbbrev() + "_val",
						bitfieldName + "_op", bitfieldName + "_op");
				method.addStatement("$L.add($L)", ftype.getAbbrev(), bitfieldName + "_op");
				method.endControlFlow();
				method.endControlFlow();
			} else {
				method.addStatement("$L = $L", ftype.getAbbrev(), retrieval);
			}
		}

		method.endControlFlow();
		method.beginControlFlow("catch ($T e)", Exception.class);
		method.addStatement("throw new $T(e)", IOException.class);
		method.endControlFlow();

		return method.build();
	}

	static FieldSpec parseField(String abbrev, FieldType ftype, String value) {
		FieldSpec.Builder builder = null;

		if (ftype.getUnit() != null && ftype.getUnit().equals("Enumerated")) {
			if (!ftype.getEnumDef().isEmpty()) {
				builder = FieldSpec.builder(ClassName.get(pkgDefs, ftype.getEnumDef()), ftype.getAbbrev(),
						Modifier.PUBLIC);
				builder.initializer("$L.values()[0]", ftype.getEnumDef());
			} else {
				builder = FieldSpec.builder(ClassName.get(pkgMsgs, abbrev, ftype.getAbbrev().toUpperCase()),
						ftype.getAbbrev(), Modifier.PUBLIC);
				builder.initializer("$L.values()[0]", ftype.getAbbrev().toUpperCase());
			}

		} else if (ftype.getUnit() != null && ftype.getUnit().equals("Bitfield")) {
			ClassName inner = ClassName.get(pkgMsgs, abbrev, ftype.getAbbrev().toUpperCase());

			if (!ftype.getBitfieldDef().isEmpty())
				inner = ClassName.get(pkgDefs, ftype.getBitfieldDef());

			ParameterizedTypeName paramType = ParameterizedTypeName.get(ClassName.get("java.util", "EnumSet"), inner);
			builder = FieldSpec.builder(paramType, ftype.getAbbrev(), Modifier.PUBLIC);
			builder.initializer("EnumSet.noneOf($T.class)", inner);
		} else {
			if (ftype.getValue() != null)
				value = ftype.getValue();
			switch (ftype.getType()) {
			case "uint8_t":
			case "uint16_t":
			case "int8_t":
			case "int16_t":
			case "int32_t":
				builder = FieldSpec.builder(int.class, ftype.getAbbrev(), Modifier.PUBLIC).initializer(value);
				break;
			case "uint64_t":
			case "int64_t":
			case "uint32_t":
				builder = FieldSpec.builder(long.class, ftype.getAbbrev(), Modifier.PUBLIC).initializer(value);
				break;
			case "fp32_t":
				builder = FieldSpec.builder(float.class, ftype.getAbbrev(), Modifier.PUBLIC)
						.initializer(value.endsWith("f") ? value : value + "f");
				break;
			case "fp64_t":
				builder = FieldSpec.builder(double.class, ftype.getAbbrev(), Modifier.PUBLIC).initializer(value);
				break;
			case "plaintext":
				if (ftype.getUnit() != null && ftype.getUnit().equals("TupleList")) {
					builder = FieldSpec.builder(TupleList.class, ftype.getAbbrev(), Modifier.PUBLIC).initializer("new TupleList($S)",
							value.equals("0") ? "" : value);
				}
				else
					builder = FieldSpec.builder(String.class, ftype.getAbbrev(), Modifier.PUBLIC).initializer("$S",
						value.equals("0") ? "" : value);
				break;
			case "rawdata":
				builder = FieldSpec.builder(byte[].class, ftype.getAbbrev(), Modifier.PUBLIC);
				builder.initializer("new byte[0]");
				break;
			case "message":
				if (ftype.getMessageType().isEmpty())
					builder = FieldSpec.builder(ClassName.get(pkgMsgs, "Message"), ftype.getAbbrev(),
							Modifier.PUBLIC);
				else
					builder = FieldSpec.builder(ClassName.get(pkgMsgs, ftype.getMessageType()), ftype.getAbbrev(),
							Modifier.PUBLIC);
				builder.initializer("null");
				break;
			case "message-list": {
				ClassName inner = ClassName.get(pkgMsgs, "Message");

				if (!ftype.getMessageType().isEmpty())
					inner = ClassName.get(pkgMsgs, ftype.getMessageType());
				ClassName outer = ClassName.get("java.util", "ArrayList");
				builder = FieldSpec.builder(ParameterizedTypeName.get(outer, inner), ftype.getAbbrev(),
						Modifier.PUBLIC);
				builder.initializer("new ArrayList<>()");
				break;
			}
			default:
				break;
			}
		}

		Builder anBuilder = AnnotationSpec.builder(pt.lsts.imc4j.annotations.FieldType.class);

		anBuilder.addMember("type", "$T.$L", IMCField.class, IMCField.getType(ftype.getType()).name());

		if (ftype.getMax() != null)
			anBuilder.addMember("max", "$L", ftype.getMax());

		if (ftype.getMin() != null)
			anBuilder.addMember("min", "$L", ftype.getMin());

		if (ftype.getUnit() != null)
			anBuilder.addMember("units", "$S", ftype.getUnit());

		builder.addAnnotation(anBuilder.build());

		if (ftype.getFieldDescription() != null)
			builder.addJavadoc(cleanJavadoc(ftype.getFieldDescription().getValue()));
		return builder.build();
	}

	static LinkedHashMap<String, TypeSpec> parseBitfields(Messages msgs) {
		LinkedHashMap<String, TypeSpec> specs = new LinkedHashMap<>();

		msgs.getBitfields().getDef().forEach(b -> {
			TypeSpec.Builder builder = TypeSpec.enumBuilder(ClassName.get(pkgDefs, b.getAbbrev()));
			b.getValue().forEach(v -> {
				builder.addEnumConstant(v.getAbbrev(), TypeSpec.anonymousClassBuilder("$Ll", v.getId()).build());
			});

			builder.addField(TypeName.LONG, "value", Modifier.PROTECTED);

			builder.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC).returns(TypeName.LONG)
					.addStatement("return $N", "value").build());

			builder.addMethod(MethodSpec.constructorBuilder().addParameter(TypeName.LONG, "value")
					.addStatement("this.$N = $N", "value", "value").build());

			MethodSpec.Builder valOf = MethodSpec.methodBuilder("valueOf")
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC).addParameter(TypeName.LONG, "value")
					.addException(IllegalArgumentException.class)
					.returns(ClassName.get(pkgDefs, b.getAbbrev()));
			valOf.beginControlFlow("for ($L v : $L.values())", b.getAbbrev(), b.getAbbrev());
			valOf.beginControlFlow("if (v.value == value)");
			valOf.addStatement("return v");
			valOf.endControlFlow();
			valOf.endControlFlow();
			valOf.addStatement("throw new IllegalArgumentException(\"Invalid value for $L: \"+value)", b.getAbbrev());
			builder.addMethod(valOf.build());

			builder.addModifiers(Modifier.PUBLIC);

			specs.put(b.getAbbrev(), builder.build());
		});

		return specs;
	}

	static LinkedHashMap<String, TypeSpec> parseEnums(Messages msgs) {
		LinkedHashMap<String, TypeSpec> specs = new LinkedHashMap<>();

		msgs.getEnumerations().getDef().forEach(b -> {
			TypeSpec.Builder builder = TypeSpec.enumBuilder(ClassName.get(pkgDefs, b.getAbbrev()));
			b.getValue().forEach(v -> {
				builder.addEnumConstant(v.getAbbrev(), TypeSpec.anonymousClassBuilder("$Ll", v.getId()).build());
			});

			builder.addField(TypeName.LONG, "value", Modifier.PROTECTED);

			builder.addMethod(MethodSpec.methodBuilder("value").addModifiers(Modifier.PUBLIC).returns(TypeName.LONG)
					.addStatement("return $N", "value").build());

			builder.addMethod(MethodSpec.constructorBuilder().addParameter(TypeName.LONG, "value")
					.addStatement("this.$N = $N", "value", "value").build());

			MethodSpec.Builder valOf = MethodSpec.methodBuilder("valueOf")
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC).addParameter(TypeName.LONG, "value")
					.addException(IllegalArgumentException.class)
					.returns(ClassName.get(pkgDefs, b.getAbbrev()));
			valOf.beginControlFlow("for ($L v : $L.values())", b.getAbbrev(), b.getAbbrev());
			valOf.beginControlFlow("if (v.value == value)");
			valOf.addStatement("return v");
			valOf.endControlFlow();
			valOf.endControlFlow();
			valOf.addStatement("throw new IllegalArgumentException(\"Invalid value for $L: \"+value)", b.getAbbrev());

			builder.addMethod(valOf.build());

			builder.addModifiers(Modifier.PUBLIC);

			specs.put(b.getAbbrev(), builder.build());
		});

		return specs;
	}

	static LinkedHashMap<String, TypeSpec> parseSupertypes(Messages msgs) {

		LinkedHashMap<String, TypeSpec> ret = new LinkedHashMap<>();

		msgs.getMessageGroups().getMessageGroup().forEach(g -> {
			TypeSpec.Builder builder = TypeSpec.classBuilder(ClassName.get(pkgMsgs, g.getAbbrev()));
			builder.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
			builder.superclass(ClassName.get(pkgMsgs, "Message"));
			TypeSpec spec = builder.build();
			g.getMessageType().forEach(t -> {
				ret.put(t.getAbbrev(), spec);
			});
		});

		return ret;
	}

	static void generateClasses(File defs, File directory) throws Exception {
		Messages proto = JAXB.unmarshal(defs, Messages.class);

		initialize(proto);

		enums.forEach((n, t) -> {
			JavaFile f = JavaFile.builder(pkgDefs, t).indent("\t").build();
			try {
				f.writeTo(new File(outDir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		bitfields.forEach((n, t) -> {
			JavaFile f = JavaFile.builder(pkgDefs, t).indent("\t").build();
			try {
				f.writeTo(new File(outDir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		JavaFile msgFile = JavaFile.builder(pkgMsgs, parseHeader(proto)).indent("\t").build();
		JavaFile factoryFile = JavaFile.builder(pkgMsgs, parseFactory(proto)).indent("\t").build();
		//JavaFile stateFile = JavaFile.builder(pkgMsgs, parseState(proto)).indent("\t").build();
		try {
			msgFile.writeTo(new File(outDir));
			factoryFile.writeTo(new File(outDir));
			//stateFile.writeTo(new File(outDir));
		} catch (Exception e) {
			e.printStackTrace();
		}

		groups.values().forEach(g -> {
			JavaFile f = JavaFile.builder(pkgMsgs, g).indent("\t").build();
			try {
				f.writeTo(new File(outDir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		proto.getMessage().forEach(m -> {
			TypeSpec spec = parseMessage(m, proto);
			JavaFile f = JavaFile.builder(pkgMsgs, spec).indent("\t").build();
			try {
				f.writeTo(new File(outDir));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		File msgsDir = new File(outDir+File.separatorChar+pkgMsgs.replace('.', File.separatorChar));
		File defsDir = new File(outDir+File.separatorChar+pkgDefs.replace('.', File.separatorChar));
		
		System.out.println("Deleting "+pkgMsgs+" folder");
		if (msgsDir.exists()) {
			for (Path p : Files.walk(msgsDir.toPath()).sorted((a, b) -> b.compareTo(a)).toArray(Path[]::new))
				Files.delete(p);
		}
		
		System.out.println("Deleting "+pkgDefs+" folder");
		if (defsDir.exists()) {
			for (Path p : Files.walk(defsDir.toPath()).sorted((a, b) -> b.compareTo(a)).toArray(Path[]::new))
				Files.delete(p);
		}
		
		generateClasses(new File("res/IMC.xml"), new File(outDir));
		System.out.println("Generation took "+(System.currentTimeMillis() - time)+" milliseconds");
	}
}
