/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.graph.mutable

import collection.mutable.HashMap
import apparat.graph.Edge

trait MutableGraphWithAdjacencyMatrix[V] extends MutableGraphLike[V]
{
	override type G = this.type

	private val adjacencyMatrix = new HashMap[V, List[E]]()
	private var edges: List[E] = Nil
	private var vertices: List[V] = Nil

	override def verticesIterator = vertices.iterator

	override def edgesIterator = edges.iterator

	override def add(edge: E): Unit = {
		assert(!contains(edge))
		assert(contains(edge.startVertex))
		assert(contains(edge.endVertex))
		adjacencyMatrix(edge.startVertex) = edge :: adjacencyMatrix(edge.startVertex)
		edges = edge :: edges
	}

	override def contains(edge: E): Boolean = edges contains edge

	override def remove(edge: E): Unit = {
		assert(contains(edge))
		assert(contains(edge.startVertex))
		assert(contains(edge.endVertex))
		adjacencyMatrix(edge.startVertex) = adjacencyMatrix(edge.startVertex) filterNot (_ == edge)
		edges = edges filterNot (_ == edge)
	}

	override def add(vertex: V): Unit = {
		assert(!contains(vertex))
		adjacencyMatrix(vertex) = Nil
		vertices = vertex :: vertices
	}

	override def contains(vertex: V): Boolean = vertices contains vertex

	override def remove(vertex: V): Unit = {
		assert(contains(vertex))
		outgoingOf(vertex) foreach remove _
		incomingOf(vertex) foreach remove _
		vertices = vertices filterNot (_ == vertex)
		adjacencyMatrix -= vertex
	}

	override def incomingOf(vertex: V) = edges filter (_.endVertex == vertex)

	override def outgoingOf(vertex: V) = adjacencyMatrix(vertex)

	override def +(edge: E) = {
		add(edge)
		this
	}

	override def -(edge: E) = {
		remove(edge)
		this
	}

	override def +(vertex: V) = {
		add(vertex)
		this
	}

	override def -(vertex: V) = {
		remove(vertex)
		this
	}

	override def replace(v0: V, v1: V) = {
		assert(contains(v0), "Graph must contain v0.")
		assert(!contains(v1), "Graph must not contain v1.")

		val oo = outgoingOf(v0)
		val io = incomingOf(v0)

		remove(v0)
		add(v1)

		for (e <- oo) add(Edge.copy(e, Some(v1)))
		for (e <- io) add(Edge.copy(e, Some(e.startVertex), Some(v1)))

		this
	}

}
