package br.com.maaswallet.trip.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringPartnerRepository extends JpaRepository<PartnerEntity, String> {
}
