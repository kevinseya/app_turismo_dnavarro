import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateCommentDto } from './dto/create-comment.dto';

@Injectable()
export class CommentsService {
  constructor(private prisma: PrismaService) {}

  async create(dto: CreateCommentDto, userId: number, postId: number) {
    return this.prisma.$transaction([
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
  }

  async findByPost(postId: number) {
    return this.prisma.comment.findMany({
      where: { postId, isActive: true },
      include: { user: true },
    });
  }
}
