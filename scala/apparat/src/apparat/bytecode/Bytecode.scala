package apparat.bytecode

import collection.mutable.ListBuffer
import operations._
import java.io.ByteArrayInputStream
import annotation.tailrec
import apparat.abc._

object Bytecode {
	def fromMethod(method: AbcMethod) = method.body match {
		case Some(body) => fromBody(body)
		case None => None
	}

	def fromBody(body: AbcMethodBody) = {
		val input = new AbcInputStream(new ByteArrayInputStream(body.code))
		val cpool = null.asInstanceOf[AbcConstantPool]
		val abc = null.asInstanceOf[Abc]
		@inline def u08 = input.readU08()
		@inline def u30 = input.readU30()
		@inline def name = cpool.names(u30)
		@inline def string = cpool.strings(u30)
		@inline def numArguments = u30
		@inline def register = u30
		@inline def slot = u30
		@inline def property = name
		@inline def nextOp: AbstractOp = input.readU08() match {
			case Op.add => Add()
			case Op.add_i => AddInt()
			case Op.add_p => error("add_p")
			case Op.applytype => ApplyType(numArguments)
			case Op.astype => AsType(name)
			case Op.astypelate => AsTypeLate()
			case Op.bitand => BitAnd()
			case Op.bitnot => BitNot()
			case Op.bitor => BitOr()
			case Op.bitxor => BitXor()
			case Op.bkpt => Breakpoint()
			case Op.bkptline => BreakpointLine()
			case Op.call => Call(numArguments)
			case Op.callmethod => CallMethod(numArguments,u30)
			case Op.callproperty => CallProperty(numArguments, property)
			case Op.callproplex => CallPropLex(numArguments, property)
			case Op.callpropvoid => CallPropVoid(numArguments, property)
			case Op.callstatic => CallStatic(numArguments, abc methods u30)
			case Op.callsuper => CallSuper(numArguments, property)
			case Op.callsupervoid => CallSuperVoid(numArguments, property)
			case Op.checkfilter => CheckFilter()
			case Op.coerce => Coerce(name)
			case Op.coerce_a => CoerceAny()
			case Op.coerce_b => error("coerce_b")
			case Op.coerce_d => error("coerce_d")
			case Op.coerce_i => error("coerce_i")
			case Op.coerce_o => error("coerce_o")
			case Op.coerce_s => CoerceString()
			case Op.coerce_u => error("coerce_u")
			case Op.construct => Construct(numArguments)
			case Op.constructprop => ConstructProp(numArguments, property)
			case Op.constructsuper => ConstructSuper(numArguments)
			case Op.convert_b => ConvertBoolean()
			case Op.convert_d => ConvertDouble()
			case Op.convert_i => ConvertInt()
			case Op.convert_m => error("convert_m")
			case Op.convert_m_p => error("convet_m_p")
			case Op.convert_o => ConvertObject()
			case Op.convert_u => ConvertUInt()
			case Op.debug => Debug(u08, string, u08, u30)
			case Op.debugfile => DebugFile(string)
			case Op.debugline => DebugLine(u30)
			case Op.declocal => DecLocal(register)
			case Op.declocal_i => DecLocalInt(register)
			case Op.declocal_p => error("declocal_p")
			case Op.decrement => Decrement()
			case Op.decrement_i => DecrementInt()
			case Op.decrement_p => error("decrement_p")
			case Op.deldescendants => error("deldescendants")
			case Op.deleteproperty => DeleteProperty(property)
			case Op.divide => Divide()
			case Op.divide_p => error("divide_p")
			case Op.dup => Dup()
			case Op.dxns => DefaultXMLNamespace(string)
			case Op.dxnslate => DefaultXMLNamespaceLate()
			case Op.equals => Equals()
			case Op.esc_xattr => EscapeXMLAttribute()
			case Op.esc_xelem => EscapeXMLElement()
			case Op.finddef => error("finddef")
			case Op.findproperty => FindProperty(property)
			case Op.findpropstrict => FindPropStrict(property)
			case Op.getdescendants => GetDescendants(property)
			case Op.getglobalscope => GetGlobalScope()
			case Op.getglobalslot => GetGlobalSlot(slot)
			case Op.getlex => GetLex(name)
			case Op.getlocal => GetLocal(register)
			case Op.getlocal0 => GetLocal(0)
			case Op.getlocal1 => GetLocal(1)
			case Op.getlocal2 => GetLocal(2)
			case Op.getlocal3 => GetLocal(3)
			case Op.getproperty => GetProperty(property)
			case Op.getscopeobject => GetScopeObject(u08)
			case Op.getslot => GetSlot(slot)
			case Op.getsuper => GetSuper(property)
			case Op.greaterequals => GreaterEquals()
			case Op.greaterthan => GreaterThan()
			case Op.hasnext => HasNext()
			case Op.hasnext2 => HasNext2(register, register)
			/*case Op.ifeq =>
			case Op.iffalse =>
			case Op.ifge =>
			case Op.ifgt =>
			case Op.ifle =>
			case Op.iflt =>
			case Op.ifne =>
			case Op.ifnge =>
			case Op.ifngt =>
			case Op.ifnle =>
			case Op.ifnlt =>
			case Op.ifstricteq =>
			case Op.ifstrictne =>
			case Op.iftrue =>*/
			case Op.in => In()
			case Op.inclocal => IncLocal(register)
			case Op.inclocal_i => IncLocalInt(register)
			case Op.inclocal_p => error("inclocal_p")
			case Op.increment => Increment()
			case Op.increment_i => IncrementInt()
			case Op.increment_p => error("increment_p")
			case Op.initproperty => InitProperty(property)
			case Op.instanceof => InstanceOf()
			case Op.istype => IsType(name)
			case Op.istypelate => IsTypeLate()
			//case Op.jump =>
			case Op.kill => Kill(register)
			case Op.label => Label()
			case Op.lessequals => LessEquals()
			case Op.lessthan => LessThan()
			case Op.lf32 => GetFloat()
			case Op.lf64 => GetDouble()
			case Op.li16 => GetShort()
			case Op.li32 => GetInt()
			case Op.li8 => GetByte()
			//case Op.lookupswitch =>
			case Op.lshift => ShiftLeft()
			case Op.modulo => Modulo()
			case Op.modulo_p => error("modulo_p")
			case Op.multiply => Multiply()
			case Op.multiply_i => MultiplyInt()
			case Op.multiply_p => error("multiply_p")
			case Op.negate => Negate()
			case Op.negate_i => NegateInt()
			case Op.negate_p => error("negate_p")
			case Op.newactivation => NewActivation()
			case Op.newarray => NewArray(numArguments)
			case Op.newcatch => NewCatch(body exceptions u30)
			case Op.newclass => NewClass(abc types u30)
			case Op.newfunction => NewFunction(abc methods u30)
			case Op.newobject => NewObject(numArguments)
			case Op.nextname => NextName()
			case Op.nextvalue => NextValue()
			case Op.nop => Nop()
			case Op.not => Not()
			case Op.pop => Pop()
			case Op.popscope => PopScope()
			case Op.pushbyte => PushByte(u08 match  {
				case x if (x & 0x80) != 0 => (x & 0x7f) - 0x80
				case y => y
			})
			case Op.pushdecimal => error("pushdecimal")
			case Op.pushdnan => error("pushdnan")//push decimal nan
			case Op.pushdouble => PushDouble(cpool doubles u30)
			case Op.pushfalse => PushFalse()
			case Op.pushint => PushInt(cpool ints u30)
			case Op.pushnamespace => PushNamespace(cpool namespaces u30)
			case Op.pushnan => PushNaN()
			case Op.pushnull => PushNull()
			case Op.pushscope => PushScope()
			case Op.pushshort => PushShort(input.readU16() match {
				case x if (x & 0x8000) != 0 => (x & 0x7fff) - 0x8000
				case y => y
			})
			case Op.pushstring => PushString(string)
			case Op.pushtrue => PushTrue()
			case Op.pushuint => PushUInt(cpool uints u30)
			case Op.pushundefined => PushUndefined()
			case Op.pushuninitialized => error("pushuninitialized")
			case Op.pushwith => PushWith()
			case Op.returnvalue => ReturnValue()
			case Op.returnvoid => ReturnVoid()
			case Op.rshift => ShiftRight()
			case Op.setglobalslot => SetGlobalSlot(slot)
			case Op.setlocal => SetLocal(register)
			case Op.setlocal0 => SetLocal(0)
			case Op.setlocal1 => SetLocal(1)
			case Op.setlocal2 => SetLocal(2)
			case Op.setlocal3 => SetLocal(3)
			case Op.setproperty => SetProperty(property)
			case Op.setslot => SetSlot(slot)
			case Op.setsuper => SetSuper(property)
			case Op.sf32 => SetFloat()
			case Op.sf64 => SetDouble()
			case Op.si16 => SetShort()
			case Op.si32 => SetInt()
			case Op.si8 => SetByte()
			case Op.strictequals => StrictEquals()
			case Op.subtract => Subtract()
			case Op.subtract_i => SubtractInt()
			case Op.subtract_p => error("subtract_p")
			case Op.swap => Swap()
			case Op.sxi1 => Sign1()
			case Op.sxi16 => Sign16()
			case Op.sxi8 => Sign8()
			case Op.`throw` => Throw()
			case Op.timestamp => error("timestamp")
			case Op.typeof => TypeOf()
			case Op.urshift => ShiftRightUnsigned()
			case x => error("Unknown opcode " + x + ".")
		}

		@tailrec def build(list: ListBuffer[AbstractOp]): ListBuffer[AbstractOp] = {
			input.available match {
				case 0 => list
				case _ => build(list += nextOp)
			}
		}

		try {
			build(new ListBuffer[AbstractOp]())
		}
		finally {
			try { input.close() } catch { case _ => {} }
		}
	}
}

class Bytecode {
	private val code = new ListBuffer[AbstractOp]()
}