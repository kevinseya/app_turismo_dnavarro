import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { Like, Post } from '@prisma/client';

@Injectable()
export class LikeService {
  constructor(private prisma: PrismaService) {}

  async like(userId: number, postId: number): Promise<[Like, Post]> {
    // Verificar si el like ya existe
    const existingLike = await this.prisma.like.findUnique({
      where: {
        userId_postId: { userId, postId },
      },
    });

    // Si ya existe, simplemente retornarlo con el post actualizado
    if (existingLike) {
      const post = await this.prisma.post.findUnique({ where: { id: postId } });
      if (!post) {
        throw new Error('Post not found');
      }
      return [existingLike, post];
    }

    // Si no existe, crearlo
    return this.prisma.$transaction([
      this.prisma.like.create({
        data: { userId, postId },
      }),
      this.prisma.post.update({
        where: { id: postId },
        data: { likesCount: { increment: 1 } },
      }),
    ]);
  }

  async unlike(userId: number, postId: number): Promise<[Like, Post]> {
    return this.prisma.$transaction([
      this.prisma.like.delete({
        where: {
          userId_postId: { userId, postId },
        },
      }),
      this.prisma.post.update({
        where: { id: postId },
        data: { likesCount: { decrement: 1 } },
      }),
    ]);
  }

  async likesCount(postId: number): Promise<number> {
    return this.prisma.like.count({
      where: { postId },
    });
  }
}
