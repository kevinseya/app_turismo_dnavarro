import { Controller, Get, Req, UseGuards, Query } from '@nestjs/common';
import { FeedService } from './feed.service';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';

@UseGuards(JwtAuthGuard)
@Controller('feed')
export class FeedController {
  constructor(private service: FeedService) {}

  @Get()
  getFeed(@Req() req) {
    return this.service.getFeed(req.user.sub);
  }

  // Feed por cercanía
  @Get('nearby')
  getNearbyFeed(
    @Query('lat') lat: string,
    @Query('lng') lng: string,
    @Query('radiusKm') radiusKm: string,
    @Query('page') page: string,
    @Query('limit') limit: string,
  ) {
    return this.service.getNearbyFeed(
      parseFloat(lat),
      parseFloat(lng),
      radiusKm ? parseFloat(radiusKm) : 10,
      page ? parseInt(page) : 1,
      limit ? parseInt(limit) : 20,
    );
  }
}
