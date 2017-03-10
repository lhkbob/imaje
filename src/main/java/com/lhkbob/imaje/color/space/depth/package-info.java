/**
 * Space.Depth
 * ===========
 *
 * The `com.lhkbob.image.color.space.depth` package defines several vector spaces for representing
 * camera {@link com.lhkbob.imaje.color.Depth depth values}. Additionally, it defines a {@link
 * com.lhkbob.imaje.color.space.depth.DepthStencilSpace} that is used to describe all spaces for
 * instances of {@link com.lhkbob.imaje.color.DepthStencil}. Every DepthStencilSpace is defined with
 * respect to a Depth vector space, and merely adds the stencil channel.
 *
 * All defined Depth vector spaces extend the abstract {@link
 * com.lhkbob.imaje.color.space.depth.DepthSpace} class, which adds transformations to the canonical
 * Scene depth space and to SRGB (for debugging or encoding purposes). The {@link
 * com.lhkbob.imaje.color.space.depth.Scene} space represents depth values directly, each pixel in
 * an image will contain the Euclidean distance to the camera plane. The {@link
 * com.lhkbob.imaje.color.space.depth.Normalized} space represents scene values normalized to the
 * range [0, 1] based on a defined near and far plane. The {@link
 * com.lhkbob.imaje.color.space.depth.ZBuffer} space is similar in concept to Normalized, but
 * represents the normalized-device coordinate and perspective division transformations that are
 * performed by a GPU when storing depth values in a depth buffer.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.depth;