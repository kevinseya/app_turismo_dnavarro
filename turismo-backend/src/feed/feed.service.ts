import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class FeedService {
  constructor(private prisma: PrismaService) {}

  async getFeed(userId: number, page = 1, limit = 20) {
    const skip = (page - 1) * limit;
    return this.prisma.post.findMany({
      where: {
        isActive: true,
        user: {
          followers: {
            some: {
              followerId: userId,
            },
          },
        },
      },
      select: {
        id: true,
        title: true,
        description: true,
        latitude: true,
        longitude: true,
        phone: true,
        likesCount: true,
        commentsCount: true,
        createdAt: true,
        user: {
          select: {
            id: true,
            name: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
      skip,
      take: limit,
    });
  }

  // Feed por cercanía
  async getNearbyFeed(lat: number, lng: number, radiusKm = 10, page = 1, limit = 20) {
    const skip = (page - 1) * limit;
    // Prisma no soporta geodistancia nativa, así que filtramos y ordenamos en JS
    const posts = await this.prisma.post.findMany({
      where: { isActive: true },
      select: {
        id: true,
        title: true,
        description: true,
        latitude: true,
        longitude: true,
        phone: true,
        likesCount: true,
        commentsCount: true,
        createdAt: true,
        user: {
          select: { id: true, name: true },
        },
      },
    });
    // Haversine formula para calcular distancia
    function getDistanceKm(lat1, lon1, lat2, lon2) {
      const R = 6371;
      const dLat = (lat2 - lat1) * Math.PI / 180;
      const dLon = (lon2 - lon1) * Math.PI / 180;
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    }
    const filtered = posts
      .map(post => ({
        ...post,
        distance: getDistanceKm(lat, lng, post.latitude, post.longitude),
      }))
      .filter(post => post.distance <= radiusKm)
      .sort((a, b) => a.distance - b.distance);
    return filtered.slice(skip, skip + limit);
  }
}
