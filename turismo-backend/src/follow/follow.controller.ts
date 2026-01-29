import { Controller, Post, Delete, Param, Req, UseGuards } from '@nestjs/common';
import { FollowService } from './follow.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@UseGuards(JwtAuthGuard)
@Controller('follow')
export class FollowController {
  constructor(private service: FollowService) {}

  @Post(':id')
  follow(@Req() req, @Param('id') id: string) {
    return this.service.follow(req.user.sub, +id);
  }

  @Delete(':id')
  unfollow(@Req() req, @Param('id') id: string) {
    return this.service.unfollow(req.user.sub, +id);
  }
}
