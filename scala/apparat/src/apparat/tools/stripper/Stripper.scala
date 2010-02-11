/*
 * This file is part of Apparat.
 *
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 *
 */
package apparat.tools.stripper

import apparat.tools.{ApparatConfiguration, ApparatApplication, ApparatTool}
import java.io.{File => JFile}
import apparat.utils.TagContainer
import actors.Futures._
import apparat.swf.{SwfTag, DoABC, SwfTags, DefineBitsLossless2}
import apparat.abc._
import apparat.bytecode.operations._
import apparat.bytecode.combinator._
import apparat.bytecode.combinator.BytecodeChains._

/**
 * @author Joa Ebert
 */
object Stripper {
	def main(args: Array[String]): Unit = ApparatApplication(new StripperTool, args)

	class StripperTool extends ApparatTool
	{
		/**
		 * The namespace of the trace() method.
		 */
		private lazy val qname = AbcQName('trace, AbcNamespace(AbcNamespaceKind.Package, Symbol("")))

		/**
		 * Rewrite rule that will replace trace(x,y) with x,y.
		 */
		private lazy val trace = {
			(FindPropStrict(qname) ~
			rep(filter {
				case CallPropVoid(name, args) if name == qname => false
				case _ => true
			}) ~
			partial {
				case CallPropVoid(name, args) if name == qname => CallPropVoid(name, args)
			}) ^^ {
				case findProp ~ ops ~ callProp if ops.length == callProp.numArguments => ops
				case findProp ~ ops ~ callProp => findProp :: ops ::: List(callProp)
				case _ => error("Internal error.")
			}
		}

		var input = ""
		var output = ""
		
		override def name = "Stripper"

		override def help = """  -i [file]	Input file
  -o [file]	Output file (optional)"""

		override def configure(config: ApparatConfiguration) = {
			input = config("-i") getOrElse error("Input is required.")
			output = config("-o") getOrElse input
			assert(new JFile(input) exists, "Input has to exist.")
		}

		override def run() = {
			SwfTags.tagFactory = (kind: Int) => kind match {
				case SwfTags.DoABC => Some(new DoABC)
				case _ => None
			}

			val source = new JFile(input)
			val target = new JFile(output)
			val cont = TagContainer fromFile source
			cont.tags = cont.tags map { tag => future { strip(tag) } } map { _() }
			cont write target
		}

		private def strip(tag: SwfTag) = tag match {
			case doABC: DoABC => {
				val abc = Abc fromDoABC doABC

				abc.loadBytecode()

				for(method <- abc.methods) {
					method.body match {
						case Some(body) => {
							body.bytecode match {
								case Some(bytecode) => bytecode rewrite trace
								case None =>
							}
						}
						case None =>
					}
				}

				abc.saveBytecode()
				abc write doABC

				doABC
			}
			case _ => tag
		}
	}
}