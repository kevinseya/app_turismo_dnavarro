import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateCommentDto } from './dto/create-comment.dto';

@Injectable()
export class CommentsService {
  constructor(private prisma: PrismaService) {}

  async create(dto: CreateCommentDto, userId: number, postId: number) {
    // Obtener información del post y del autor del comentario
    const post = await this.prisma.post.findUnique({
      where: { id: postId },
      include: { user: true }
    });

    const commenter = await this.prisma.user.findUnique({
      where: { id: userId }
    });

    // Crear el comentario y actualizar el contador
    const [comment] = await this.prisma.$transaction([
      this.prisma.comment.create({
        data: {
          content: dto.content,
          rating: dto.rating,
          userId,
          postId,
        },
      }),
      this.prisma.post.update({
        where: { id: postId },
        data: { commentsCount: { increment: 1 } },
      }),
    ]);

    // Crear notificación para el autor del post (si no es el mismo que comenta)
    if (post && post.userId !== userId) {
      await this.prisma.notification.create({
        data: {
          title: 'Nuevo comentario',
          message: `${commenter?.name || 'Alguien'} comentó en tu publicación: "${dto.content.substring(0, 50)}${dto.content.length > 50 ? '...' : ''}"`,
          type: 'comment',
          userId: post.userId,
          postId: postId,
          commentId: comment.id,
        },
      });
    }

    return comment;
  }

  async findByPost(postId: number) {
    return this.prisma.comment.findMany({
      where: { postId, isActive: true },
      include: { user: true },
      orderBy: { createdAt: 'desc' }
    });
  }

  async remove(commentId: number, postId: number, userId: number) {
    // Verificar que el usuario sea admin o propietario del comentario
    const comment = await this.prisma.comment.findUnique({
      where: { id: commentId }
    });

    const user = await this.prisma.user.findUnique({
      where: { id: userId }
    });

    if (!comment) {
      throw new Error('Comentario no encontrado');
    }

    // Solo admin o propietario del comentario pueden borrar
    if (user?.role !== 'ADMIN' && comment.userId !== userId) {
      throw new Error('No tienes permiso para borrar este comentario');
    }

    // Soft delete del comentario y decrementar contador
    await this.prisma.$transaction([
      this.prisma.comment.update({
        where: { id: commentId },
        data: { isActive: false }
      }),
      this.prisma.post.update({
        where: { id: postId },
        data: { commentsCount: { decrement: 1 } }
      })
    ]);

    return { message: 'Comentario eliminado correctamente' };
  }
}
