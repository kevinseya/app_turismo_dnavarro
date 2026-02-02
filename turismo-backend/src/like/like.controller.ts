import { Controller, Post, Delete, Param, Req, UseGuards } from '@nestjs/common';
import { LikeService } from './like.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@UseGuards(JwtAuthGuard)
@Controller('likes')
export class LikeController {
  constructor(private service: LikeService) {}

  @Post(':postId')
  like(@Req() req, @Param('postId') postId: string) {
    return this.service.like(req.user.userId, +postId);
  }

  @Delete(':postId')
  unlike(@Req() req, @Param('postId') postId: string) {
    return this.service.unlike(req.user.userId, +postId);
  }
}
