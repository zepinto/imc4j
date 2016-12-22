/*
 * Below is the copyright agreement for IMCJava.
 * 
 * Copyright (c) 2010-2016, Laboratório de Sistemas e Tecnologia Subaquática
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of IMC, LSTS, IMCJava nor the names of its 
 *       contributors may be used to endorse or promote products derived from 
 *       this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL LABORATORIO DE SISTEMAS E TECNOLOGIA SUBAQUATICA
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * $Id:: IMCFieldType.java 334 2013-01-02 11:20:47Z zepinto                    $:
 */
package pt.lsts.imc4j.annotations;

import java.util.LinkedHashMap;

/**
 * @author zp
 *
 */
public enum IMCField {

	TYPE_UINT8("uint8_t", 1),
	TYPE_UINT16("uint16_t", 2),
	TYPE_UINT32("uint32_t", 4),
	TYPE_INT8("int8_t", 1),
	TYPE_INT16("int16_t", 2),
	TYPE_INT32("int32_t", 4),
	TYPE_INT64("int64_t", 8),
	TYPE_FP32("fp32_t", 4),
	TYPE_FP64("fp64_t", 8),
	TYPE_RAWDATA("rawdata", -1),
	TYPE_PLAINTEXT("plaintext", -1),
	TYPE_MESSAGE("message", -1),
	TYPE_MESSAGELIST("message-list", -1);
	
	private String name;
	private int size;
	
	private IMCField(String name, int size) {
		this.name = name;
		this.size = size;
	}

	public String getTypeName() {
		return name;
	}
	
	public int getSizeInBytes() {
		return size;
	}
	
	public boolean isSizeKnown() {
		return size != -1;
	}
	
	public String toString() {
		return name;
	}
	
	
	static LinkedHashMap<String, IMCField> types = new LinkedHashMap<String, IMCField>();
	
	static {
		types.put("uint8_t", IMCField.TYPE_UINT8);
		types.put("uint16_t", IMCField.TYPE_UINT16);
		types.put("uint32_t", IMCField.TYPE_UINT32);
		types.put("int8_t", IMCField.TYPE_INT8);
		types.put("int16_t", IMCField.TYPE_INT16);
		types.put("int32_t", IMCField.TYPE_INT32);
		types.put("int64_t", IMCField.TYPE_INT64);
		types.put("fp32_t", IMCField.TYPE_FP32);
		types.put("fp64_t", IMCField.TYPE_FP64);
		types.put("message", IMCField.TYPE_MESSAGE);
		types.put("plaintext", IMCField.TYPE_PLAINTEXT);
		types.put("rawdata", IMCField.TYPE_RAWDATA);
		types.put("message-list", IMCField.TYPE_MESSAGELIST);
	}	
	
	public static IMCField getType(String typeName) {
		return types.get(typeName);
	}
}
